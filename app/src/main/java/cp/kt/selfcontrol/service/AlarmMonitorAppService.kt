package cp.kt.selfcontrol.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import com.jeremyliao.liveeventbus.LiveEventBus
import cp.kt.selfcontrol.receiver.AlarmMonitorAppReceiver
import cp.kt.selfcontrol.util.Constant
import java.util.*


class AlarmMonitorAppService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAlarm()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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

        val packageName = queryUsageStats(this)
        packageName?.let {
            LiveEventBus.get(Constant.EventBus.APP_STATE_CHANGED).post(it)
        }
    }

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
}