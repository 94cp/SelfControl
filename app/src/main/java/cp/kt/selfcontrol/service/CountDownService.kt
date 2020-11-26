package cp.kt.selfcontrol.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.jeremyliao.liveeventbus.LiveEventBus
import cp.kt.selfcontrol.util.Constant

class CountDownService : Service() {
    private var running: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            val countdown = intent?.getLongExtra(Constant.Extra.COUNTDOWN, 0) ?: 0

            if (countdown >= 1000) {
                startCountdown(countdown)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startCountdown(countdown: Long, interval: Long = 1000) {
        object : CountDownTimer(countdown, interval) {
            override fun onTick(millisInFuture: Long) {
                LiveEventBus.get(Constant.EventBus.COUNTDOWN_CHANGED).post(millisInFuture)
            }

            override fun onFinish() {
                running = false
                LiveEventBus.get(Constant.EventBus.COUNTDOWN_CHANGED).post(0)
            }
        }.start()
        running = true
    }
}