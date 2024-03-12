package app.grapheneos.setupwizard.view.activity

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.MainThread
import app.grapheneos.setupwizard.R
import app.grapheneos.setupwizard.action.WelcomeActions

class OemUnlockActivity :
    SetupWizardActivity(
        R.layout.activity_oem_unlock,
        R.drawable.baseline_warning_amber_orange_glif,
        R.string.lock_your_bootloader
    ) {
    companion object {
        private const val TAG = "OemUnlockActivity"
    }

    private lateinit var acknowledgeRisks: CheckBox
    private lateinit var continueUnlocked: View
    private lateinit var rebootToBootloader: TextView

    @MainThread
    override fun bindViews() {
        acknowledgeRisks = requireViewById(R.id.acknowledge_risks)
        continueUnlocked = requireViewById(R.id.continue_unlocked)
        rebootToBootloader = requireViewById(R.id.reboot_to_bootloader)
    }

    @MainThread
    override fun setupActions() {
        acknowledgeRisks.setOnClickListener {
            continueUnlocked.isEnabled = acknowledgeRisks.isChecked
        }
        continueUnlocked.setOnClickListener { WelcomeActions.next(this) }
        rebootToBootloader.setOnClickListener { WelcomeActions.rebootBootloader() }
    }
}
