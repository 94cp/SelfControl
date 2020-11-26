package cp.kt.selfcontrol.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.jeremyliao.liveeventbus.LiveEventBus
import cp.kt.selfcontrol.util.Constant

class MonitorAppService : AccessibilityService() {
    // 接收到系统发送AccessibilityEvent时的回调
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        event?.let {
            val packageName = it.packageName.toString()
            LiveEventBus.get(Constant.EventBus.APP_STATE_CHANGED).post(packageName)
        }
    }

    // 服务中断时的回调
    override fun onInterrupt() {}
}