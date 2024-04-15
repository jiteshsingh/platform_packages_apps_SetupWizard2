package app.grapheneos.setupwizard.action

import androidx.annotation.MainThread
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER
import app.grapheneos.setupwizard.appContext
import app.grapheneos.setupwizard.data.OemUnlockData
import app.grapheneos.setupwizard.view.activity.OemUnlockActivity
import app.grapheneos.setupwizard.view.activity.WelcomeActivity

object OemUnlockActions {
    private const val TAG = "OemUnlockActions"
    private var ackTimerStarted = false

    fun rebootToBootloader() {
        WelcomeActions.rebootToBootloader()
    }

    fun next(activity: OemUnlockActivity) {
        SetupWizard.next(activity, WelcomeActivity::class.java)
        return
    }

    @MainThread
    fun startAckTimer(time: Int = OEM_UNLOCKED_ACK_TIMER) {
        if (ackTimerStarted) return
        ackTimerStarted = true
        runAckTimer(time)
    }

    @MainThread
    private fun runAckTimer(time: Int) {
        if (time <= 0) return
        appContext.mainThreadHandler.postDelayed({
            OemUnlockData.ackTimer.value = time - 1
            runAckTimer(time - 1)
        }, 1_000)
    }
}
