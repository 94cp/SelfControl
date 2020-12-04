# SelfControl

## 功能

- 支持黑暗模式
- 支持小狗、小松鼠、小兔子3种监控宠物
- 支持应用白名单（默认系统应用+本应用）
- 支持省电/省心2种监控模式（省电模式每次都要授权，较繁琐；省心模式仅需授权一次，但较耗电）
- 支持自动作诗
- 使用Kotlin重写，代码更健壮

## 关键技术点

- 如何判断APP前后台状态（UsageStatsManager 或 AccessibilityService）
- 如何关闭其它APP

## 六种判断APP前后台状态方法
-----

| 方法  | 原理                       | 权限 | 是否可判断其它APP前后台状态      | 特点                                                         |
| ----- | -------------------------- | ---- | -------------------------------- | ------------------------------------------------------------ |
| 方法1 | RunningTask                | 否   | Android 5.0以上不行              | 5.0此方法被废弃                                              |
| 方法2 | RunningProcess             | 否   | 当APP存在后台常驻的Service时失效 | 无                                                           |
| 方法3 | ActivityLifecycleCallbacks | 否   | 否                               | 简单有效，代码最少                                           |
| 方法4 | 读取/proc目录下的信息      | 否   | Android 7.0以上不行              | 7.0谷歌限制了/proc目录的访问<br />当proc目录下文件夹过多时，过多的IO操作会引起耗时 |
| 方法5 | UsageStatsManager          | 是   | 是                               | 仅首次需要用户授权权限，最符合Google规范的判断方法           |
| 方法6 | AccessibilityService       | 否   | 是                               | 需要用户授权辅助功能，会伴随应用被“强行停止”而剥夺辅助功能，导致需要用户重新授权辅助功能 |

具体各个方法的优缺点可以参考[AndroidProcess](https://github.com/wenmingvs/AndroidProcess)，这里不予展开详述。

由于其它方式都无法很好地判断其它APP的前后台状态，故仅选用了方法5`UsageStatsManager`和方法6`AccessibilityService`。

## UsageStatsManager

通过UsageStatsManager可以获取一个时间段内的应用统计信息，利用此方式可以间接获取APP前后台状态。

前提：

1. Android5.0以上

2. ```xml
   <uses-permission  android:name="android.permission.PACKAGE_USAGE_STATS" />
   ```

关键代码：

```kotlin
// 获取APP前后台状态
private fun queryUsageStats(context: Context): String? {
    class RecentUseComparator : Comparator<UsageStats> {
        override fun compare(lhs: UsageStats, rhs: UsageStats): Int {
            return if (lhs.lastTimeUsed > rhs.lastTimeUsed) -1 else if (lhs.lastTimeUsed == rhs.lastTimeUsed) 0 else 1
        }
    }

    val mRecentComp = RecentUseComparator()
    val ts = System.currentTimeMillis()
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000 * 10, ts)
    if (usageStats == null || usageStats.size == 0) {
        return null
    }
    Collections.sort(usageStats, mRecentComp)
    return usageStats[0].packageName
}
```

```kotlin
// 开启闹钟服务定时查询APP前后台状态
private fun startAlarm(interval: Long = 1000) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val triggerAtTime = SystemClock.elapsedRealtime() + interval
    val i = Intent(this, AlarmMonitorAppReceiver::class.java)
    val pi = PendingIntent.getBroadcast(this, 0, i, 0)

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        triggerAtTime,
        pi
    )
	  // 前台APP包名
    val packageName = queryUsageStats(this)
    // TODO   
}
```

但随着Google对API的收紧，AlarmManager仅会运行一次，所以需要通过下面的广播接收者重新激活服务

```kotlin
// 重新激活闹钟服务
class AlarmMonitorAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, AlarmMonitorAppService::class.java)
        context?.startService(i)
    }
}
```

## AccessibilityService

Android 辅助功能(AccessibilityService) 为我们提供了一系列的事件回调，帮助我们指示一些用户界面的状态变化。 我们可以派生辅助功能类，进而对不同的 AccessibilityEvent 进行处理。 同样的，这个服务就可以用来判断当前的前台应用。且不再需要轮询的判断当前的应用是不是在前台，系统会在窗口状态发生变化的时候主动回调，耗时和资源消耗都极小。

关键代码：

```xml
<!-- 注册服务 -->
<service
    android:name=".service.MonitorAppService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService"/>
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessible_service_monitor_app_config"/>
</service>
```

```kotlin
class MonitorAppService : AccessibilityService() {
    // 接收到系统发送AccessibilityEvent时的回调
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        event?.let {
          	// 前台APP包名
            val packageName = it.packageName.toString()
            // TODO
        }
    }
}
```

```xml
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/monitor_app_accessibility_desc"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackAllMask"
    android:accessibilityFlags="flagIncludeNotImportantViews" />
    <!--
    accessibilityEventTypes: 监听的事件类型. typeWindowStateChanged: 监听窗口状态变化
    accessibilityFeedbackType: 反馈类型. feedbackAllMask: 所有的可用反馈类型
    accessibilityFlags: 辅助功能附加的标志. flagIncludeNotImportantViews: 可获取到一些被表示为辅助功能无权获取到的view
    -->
```

## 关闭其它APP

```kotlin
// 实际上我们无法关闭其它APP，但可以通过模拟点击Home键，将屏幕退回到主界面，以此模拟实现关闭效果
private fun closeApp() {
    val intent = Intent()
    intent.action = Intent.ACTION_MAIN
    intent.addCategory(Intent.CATEGORY_HOME)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    MainApplication.context.startActivity(intent)
}
```

## AccessibilityService的一些其它骚操作

### 抢红包

抢红包流程：

1. 状态栏出现"[微信红包]"的消息提示,点击进入聊天界面
2. 点击相应的红包信息,弹出抢红包界面
3. 在抢红包界面点击"开",打开红包
4. 在红包详情页面,查看详情,点击返回按钮返回微信聊天界面.

```java
// 简化抢红包核心代码
public class RobService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    getPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    openPacket();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    close();
                }
                break;
        }
    }

    /** 处理通知栏信息 如果是微信红包的提示信息,则模拟点击 */
    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                if (content.contains("[微信红包]")) {
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /** 关闭红包详情界面,实现自动返回聊天窗口 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void close() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            //为了演示,直接查看了关闭按钮的id
            List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId("@id/ez");
            nodeInfo.recycle();
            for (AccessibilityNodeInfo item : infos) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /** 模拟点击,拆开红包 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            //为了演示,直接查看了红包控件的id
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("@id/b9m");
            nodeInfo.recycle();
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /** 模拟点击,打开抢红包界面 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        AccessibilityNodeInfo node = recycle(rootNode);

        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        AccessibilityNodeInfo parent = node.getParent();
        while (parent != null) {
            if (parent.isClickable()) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            parent = parent.getParent();
        }

    }

    /** 递归查找当前聊天窗口中的红包信息 聊天窗口中的红包都存在"领取红包"一词,因此可根据该词查找红包 */
    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node) {
        if (node.getChildCount() == 0) {
            if (node.getText() != null) {
                if ("领取红包".equals(node.getText().toString())) {
                    return node;
                }
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    recycle(node.getChild(i));
                }
            }
        }
        return node;
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }
}
```

### 自动安装APP

安装APP流程：

点击apk文件，弹出安装信息界面，在该界面点击"下一步"，然后在点击"安装"，最后在安装完成界面点击"完成".

```java
public class InstallService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("InstallService", event.toString());
        checkInstall(event);
    }

    private void checkInstall(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source != null) {
            boolean installPage = event.getPackageName().equals("com.android.packageinstaller");
            if (installPage) {
                installAPK(event);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void installAPK(AccessibilityEvent event) {
        AccessibilityNodeInfo source = getRootInActiveWindow();
        List<AccessibilityNodeInfo> nextInfos = source.findAccessibilityNodeInfosByText("下一步");
        nextClick(nextInfos);
        List<AccessibilityNodeInfo> installInfos = source.findAccessibilityNodeInfosByText("安装");
        nextClick(installInfos);
        List<AccessibilityNodeInfo> openInfos = source.findAccessibilityNodeInfosByText("打开");
        nextClick(openInfos);

        runInBack(event);

    }

    private void runInBack(AccessibilityEvent event) {
        event.getSource().performAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private void nextClick(List<AccessibilityNodeInfo> infos) {
        if (infos != null)
            for (AccessibilityNodeInfo info : infos) {
                if (info.isEnabled() && info.isClickable())
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean checkTilte(AccessibilityNodeInfo source) {
        List<AccessibilityNodeInfo> infos = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("@id/app_name");
        for (AccessibilityNodeInfo nodeInfo : infos) {
            if (nodeInfo.getClassName().equals("android.widget.TextView")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
    }
}
```

### 领喵币

见：[双十一领喵币](https://github.com/tyhjh/TmallCoin)

### 窃取与反窃取

窃取：如窃取短信验证码，窃取短信内容等等

反窃取：如根据抢红包插件的原理，利用AccessibilityService发送虚假微信红包通知，就可以让抢红包插件失效

## 三方库

- [FloatWindow 安卓任意界面悬浮窗](https://github.com/yhaolpz/FloatWindow)
- [LiveEventBus是一款Android消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX，支持跨进程，支持跨APP](https://github.com/JeremyLiao/LiveEventBus)
