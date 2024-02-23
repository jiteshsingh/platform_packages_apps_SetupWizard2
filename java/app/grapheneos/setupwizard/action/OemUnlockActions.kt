package app.grapheneos.setupwizard.action

import androidx.annotation.WorkerThread
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER
import app.grapheneos.setupwizard.android.background
import app.grapheneos.setupwizard.android.foreground
import app.grapheneos.setupwizard.data.OemUnlockData
import app.grapheneos.setupwizard.view.activity.OemUnlockActivity
import app.grapheneos.setupwizard.view.activity.WelcomeActivity

object OemUnlockActions {
    private const val TAG = "OemUnlockActions"

    private val timerLock = Any()
    private var timerStarted = false

    fun rebootToBootloader() {
        WelcomeActions.rebootToBootloader()
    }

    fun next(activity: OemUnlockActivity) {
        SetupWizard.next(activity, WelcomeActivity::class.java)
        return
    }

    fun startAckTimer() {
        if (timerStarted) return
        synchronized(timerLock) {
            timerStarted = true
            background { runAckTimer() }
        }
    }

    @WorkerThread
    private fun runAckTimer() {
        for (timer in OEM_UNLOCKED_ACK_TIMER downTo 0) {
            foreground { OemUnlockData.ackTimer.value = timer }
            Thread.sleep(1_000)
        }
    }
}
