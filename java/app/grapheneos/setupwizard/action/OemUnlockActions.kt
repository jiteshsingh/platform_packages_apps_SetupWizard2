package app.grapheneos.setupwizard.action

import androidx.annotation.MainThread
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER
import app.grapheneos.setupwizard.appContext
import app.grapheneos.setupwizard.data.OemUnlockData
import app.grapheneos.setupwizard.view.activity.OemUnlockActivity
import app.grapheneos.setupwizard.view.activity.WelcomeActivity

object OemUnlockActions {
    private const val TAG = "OemUnlockActions"
    private var ackTimerRunning = false

    fun rebootToBootloader() {
        WelcomeActions.rebootToBootloader()
    }

    fun next(activity: OemUnlockActivity) {
        SetupWizard.next(activity, WelcomeActivity::class.java)
        return
    }

    @MainThread
    fun startAckTimer(time: Int = OEM_UNLOCKED_ACK_TIMER) {
        if (ackTimerRunning) return
        ackTimerRunning = true
        runAckTimer(time)
    }

    @MainThread
    fun stopAckTimer() {
        ackTimerRunning = false
    }

    @MainThread
    private fun runAckTimer(time: Int) {
        if (time <= 0) return
        if (!ackTimerRunning) {
            // timer was interrupted
            OemUnlockData.ackTimer.value = OEM_UNLOCKED_ACK_TIMER // reset timer value
            return
        }
        appContext.mainThreadHandler.postDelayed({
            OemUnlockData.ackTimer.value = time - 1
            runAckTimer(time - 1)
        }, 1_000)
    }
}
