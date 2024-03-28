package app.grapheneos.setupwizard.view.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.MainThread

import com.google.android.setupcompat.template.FooterButtonStyleUtils
import com.google.android.setupcompat.util.WizardManagerHelper
import com.google.android.setupdesign.GlifLayout

import app.grapheneos.setupwizard.R
import app.grapheneos.setupwizard.action.FinishActions
import app.grapheneos.setupwizard.action.SetupWizard
import app.grapheneos.setupwizard.action.WelcomeActions
import app.grapheneos.setupwizard.android.foreground
import app.grapheneos.setupwizard.appContext
import app.grapheneos.setupwizard.data.WelcomeData
import app.grapheneos.setupwizard.setText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO: explore Material 3.0 with JetPack compose
class WelcomeActivity : SetupWizardActivity(R.layout.activity_welcome) {
    companion object {
        private const val TAG = "WelcomeActivity"
    }

    private lateinit var oemUnlockedContainer: View
    private lateinit var language: TextView
    private lateinit var accessibility: View
    private lateinit var letsSetupText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true)
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


        // without the abstraction
        appContext.mainThreadHandler.post { }
        appContext.mainThreadHandler.postDelayed({ }, 1_000)
        language.post { }
        language.postDelayed({ }, 1_000)

        // with the abstraction
        foreground { }
        foreground(1_000) {}


        oemUnlockedContainer = requireViewById(R.id.oem_unlocked_container)
        language = requireViewById(R.id.language)
        accessibility = requireViewById(R.id.accessibility)
        letsSetupText = requireViewById(R.id.lets_setup_text)
        letsSetupText.setText(
            if (SetupWizard.isPrimaryUser) R.string.lets_setup_your_device
            else R.string.lets_setup_your_profile
        )
        secondaryButton.setText(R.string.emergency_call)
        WelcomeData.selectedLanguage.observe(this) {
            Log.d(TAG, "selectedLanguage: ${it.displayName}")
            this.language.text = it.displayName
        }
        WelcomeData.oemUnlocked.observe(this) {
            Log.d(TAG, "oemUnlocked: $it")
            oemUnlockedContainer.visibility = if (it) View.VISIBLE else View.GONE
        }
//        primaryButton.isEnabled = false
//        secondaryButton.isEnabled = false
    }

    @MainThread
    override fun setupActions() {
        language.setOnClickListener { WelcomeActions.showLanguagePicker(this) }
        accessibility.setOnClickListener { WelcomeActions.accessibilitySettings(this) }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CALLING)) {
            secondaryButton.setOnClickListener { WelcomeActions.emergencyCall(this) }
        } else {
            secondaryButton.visibility = View.GONE
        }
        primaryButton.setOnClickListener { WelcomeActions.next(this) }
    }
}
