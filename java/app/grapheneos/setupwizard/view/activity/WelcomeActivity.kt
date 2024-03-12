package app.grapheneos.setupwizard.view.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER

import com.google.android.setupcompat.util.WizardManagerHelper

import app.grapheneos.setupwizard.R
import app.grapheneos.setupwizard.action.FinishActions
import app.grapheneos.setupwizard.action.SetupWizard
import app.grapheneos.setupwizard.action.WelcomeActions
import app.grapheneos.setupwizard.data.WelcomeData
import com.google.android.setupdesign.GlifLayout

// TODO: explore Material 3.0 with JetPack compose
class WelcomeActivity : SetupWizardActivity(R.layout.activity_welcome) {
    companion object {
        private const val TAG = "WelcomeActivity"
    }

    private lateinit var glifLayout: GlifLayout
    private lateinit var oemUnlockedSummaryContainer: View
    private lateinit var oemUnlockedDetailContainer: View
    private lateinit var primaryContentContainer: View
    private lateinit var language: TextView
    private lateinit var accessibility: View
    private lateinit var acknowledgeRisks: CheckBox
    private lateinit var secondaryButton: TextView
    private lateinit var primaryButton: TextView
    private lateinit var letsSetupText: TextView
    private lateinit var defaultSecondaryButtonTextColor: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        if (WizardManagerHelper.isUserSetupComplete(this)) {
            superOnCreateAtBaseClass(savedInstanceState)
            FinishActions.finish(this)
            return
        }
        WelcomeActions.handleEntry(this)
        super.onCreate(savedInstanceState)
    }

    @MainThread
    override fun bindViews() {
        glifLayout = requireViewById(R.id.glif_layout)
        oemUnlockedSummaryContainer = requireViewById(R.id.oem_unlocked_summary_container)
        oemUnlockedDetailContainer = requireViewById(R.id.oem_unlocked_detail_container)
        primaryContentContainer = requireViewById(R.id.primary_content_container)
        language = requireViewById(R.id.language)
        accessibility = requireViewById(R.id.accessibility)
        acknowledgeRisks = requireViewById(R.id.acknowledge_risks)
        secondaryButton = requireViewById(R.id.secondary_button)
        primaryButton = requireViewById(R.id.primary_button)
        letsSetupText = requireViewById(R.id.lets_setup_text)
        defaultSecondaryButtonTextColor = secondaryButton.textColors
        letsSetupText.setText(
            if (SetupWizard.isPrimaryUser) R.string.lets_setup_your_device
            else R.string.lets_setup_your_profile
        )
        WelcomeData.selectedLanguage.observe(this) {
            Log.d(TAG, "selectedLanguage: ${it.displayName}")
            this.language.text = it.displayName
        }
        WelcomeData.displayPrimaryContent.observe(this) { primary ->
            primaryContentContainer.isVisible = primary
            oemUnlockedSummaryContainer.isVisible = primary && WelcomeData.oemUnlocked.value == true
            secondaryButton.isVisible = !primary || WelcomeData.hasTelephonyFeature
            if (primary) {
                secondaryButton.setText(R.string.emergency_call)
                primaryButton.setText(R.string.next)
                secondaryButton.isVisible = WelcomeData.hasTelephonyFeature
                glifLayout.icon = null
                glifLayout.headerText = null
            }
        }
        WelcomeData.displayOemUnlockedAck.observe(this) { ack ->
            oemUnlockedDetailContainer.isVisible = ack
            acknowledgeRisks.isVisible = ack
            if (ack) {
                updateAckButton(OEM_UNLOCKED_ACK_TIMER)
                primaryButton.setText(R.string.reboot_to_bootloader)
                secondaryButton.isVisible = true
                glifLayout.icon =
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.baseline_warning_amber_orange_glif
                    )
                glifLayout.setHeaderText(R.string.lock_your_bootloader)
            }
        }
        WelcomeData.oemUnlockedAckTimer.observe(this) {
            if (WelcomeData.displayOemUnlockedAck.value == false) return@observe
            updateAckButton(it)
        }
    }

    private fun updateAckButton(timer: Int) {
        val text = if (timer == 0) {
            getString(R.string.continue_without_unlocking)
        } else {
            getString(R.string.continue_without_unlocking_timer, timer)
        }
        secondaryButton.text = text
        val enable = timer == 0 && acknowledgeRisks.isChecked
        secondaryButton.isEnabled = enable
        if (enable) {
            secondaryButton.setTextColor(
                resources.getColor(R.color.oem_unlocked_warning_color, theme)
            )
        } else {
            secondaryButton.setTextColor(defaultSecondaryButtonTextColor)
        }
    }

    @MainThread
    override fun setupActions() {
        language.setOnClickListener { WelcomeActions.showLanguagePicker(this) }
        accessibility.setOnClickListener { WelcomeActions.accessibilitySettings(this) }
        secondaryButton.setOnClickListener {
            if (WelcomeData.displayPrimaryContent.value == true) {
                WelcomeActions.emergencyCall(this)
            } else if (WelcomeData.displayOemUnlockedAck.value == true) {
                SetupWizard.next(this)
            }
        }
        primaryButton.setOnClickListener {
            if (WelcomeData.displayPrimaryContent.value == true) {
                WelcomeActions.next(this)
            } else if (WelcomeData.displayOemUnlockedAck.value == true) {
                WelcomeActions.rebootBootloader()
            }
        }
        acknowledgeRisks.setOnClickListener {
            if (WelcomeData.oemUnlockedAckTimer.value!! > 0) return@setOnClickListener
            updateAckButton(0)
        }
    }
}
