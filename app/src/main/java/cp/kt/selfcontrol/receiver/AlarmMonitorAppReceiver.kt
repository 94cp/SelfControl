package cp.kt.selfcontrol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cp.kt.selfcontrol.service.AlarmMonitorAppService

class AlarmMonitorAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, AlarmMonitorAppService::class.java)
        context?.startService(i)
    }
}