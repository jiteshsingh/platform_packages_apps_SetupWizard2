package app.grapheneos.setupwizard.action

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.service.oemlock.OemLockManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import app.grapheneos.setupwizard.APPLY_SIM_LANGUAGE_ON_ENTRY
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER
import app.grapheneos.setupwizard.R
import app.grapheneos.setupwizard.android.background
import app.grapheneos.setupwizard.android.foreground
import app.grapheneos.setupwizard.appContext
import app.grapheneos.setupwizard.data.WelcomeData
import com.android.internal.app.LocalePicker
import com.android.internal.app.LocalePicker.LocaleInfo
import com.google.android.setupcompat.util.SystemBarHelper
import java.util.Locale

object WelcomeActions {
    private const val TAG = "WelcomeActions"
    private const val ACTION_ACCESSIBILITY = "android.settings.ACCESSIBILITY_SETTINGS_FOR_SUW"
    private const val ACTION_EMERGENCY = "com.android.phone.EmergencyDialer.DIAL"
    private const val REBOOT_REASON_BOOTLOADER = "bootloader"
    private var simLocaleApplied = false

    init {
        refreshCurrentLocale()
        refreshOemUnlockStatus()
        showPrimaryContent(true)
        Log.d(TAG, "init: currentLocale = ${WelcomeData.selectedLanguage}")
    }

    fun handleEntry(context: Activity) {
        SetupWizard.setStatusBarHidden(true)
        SystemBarHelper.setBackButtonVisible(context.window, false)
        if (APPLY_SIM_LANGUAGE_ON_ENTRY) applySimLocale()
    }

    fun showLanguagePicker(activity: Activity) {
        val adapter = constructLocaleAdapter(activity)
        AlertDialog.Builder(activity)
            .setTitle(R.string.choose_your_language)
            .setAdapter(adapter) { _, which ->
                updateLocale(
                    adapter.getItem(which)!!.locale
                )
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun applySimLocale() {
        if (simLocaleApplied) return
        val simLocale = getSimLocale() ?: return
        updateLocale(simLocale)
        simLocaleApplied = true
    }

    private fun updateLocale(locale: Locale) {
        LocalePicker.updateLocale(locale)
        refreshCurrentLocale()
    }

    private fun refreshCurrentLocale() {
        WelcomeData.selectedLanguage.value = LocalePicker.getLocales()[0]
    }

    fun accessibilitySettings(context: Activity) {
        SetupWizard.startActivity(context, Intent(ACTION_ACCESSIBILITY))
    }

    fun emergencyCall(context: Activity) {
        SetupWizard.startActivity(context, Intent(ACTION_EMERGENCY))
    }

    private fun getSimLocale(): Locale? {
        return appContext.getSystemService(TelephonyManager::class.java)!!.simLocale
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun constructLocaleAdapter(activity: Activity): ArrayAdapter<LocaleInfo> {
        val adapter = LocalePicker.constructAdapter(activity)
        val simLocale = getSimLocale() ?: return adapter
        var localeInfo: LocaleInfo? = null
        for (index in 0..<adapter.count) {
            val item = adapter.getItem(index)
            if (!item?.locale?.toLanguageTag().equals(simLocale.toLanguageTag())) continue
            Log.d(TAG, "constructLocaleAdapter: found simLocale $simLocale")
            localeInfo = item
            adapter.remove(localeInfo)
            break
        }
        if (localeInfo != null) adapter.insert(localeInfo, 0)
        return adapter
    }

    private fun disableOemUnlockByUser() {
        getOemLockManager()?.isOemUnlockAllowedByUser = false
        refreshOemUnlockStatus()
    }

    private fun getOemLockManager(): OemLockManager? {
        return appContext.getSystemService(OemLockManager::class.java)
    }

    fun rebootBootloader() {
        appContext.getSystemService(PowerManager::class.java)!!.reboot(REBOOT_REASON_BOOTLOADER)
    }

    fun next(activity: Activity) {
        if (Build.isDebuggable() && false) {
            // we allow free pass for development features on debug builds of the OS
            SetupWizard.next(activity)
            return
        }
        if (SetupWizard.isSecondaryUser) {
            // secondary users should not be bothered for this as they're not admins
            // and the device setup (primary user setup) is already done at this point
            SetupWizard.next(activity)
            return
        }
        if (WelcomeData.oemUnlocked.value == true) {
            handleOemUnlocked(activity)
        } else if (WelcomeData.oemUnlockingEnabled.value == true) {
            handleEnabledOemUnlocking(activity)
        } else {
            SetupWizard.next(activity)
        }
    }

    private fun handleEnabledOemUnlocking(activity: Activity) {
        // suggest the user to disable oem unlocking if enabled
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.confirmation)
            .setMessage(R.string.oem_unlock_disabling_suggestion)
            .setPositiveButton(R.string.yes_disable_now) { _, _ -> disableOemUnlockByUser() }
            .setNegativeButton(R.string.no_disable_later) { _, _ -> }
            .setOnDismissListener { SetupWizard.next(activity) }
            .setCancelable(false)
            .create()
        dialog.show()
        warnNegativeButton(dialog, activity)
    }

    private fun handleOemUnlocked(activity: Activity) {
        // suggest the user to reboot to fastboot where they can lock bootloader
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.confirmation)
            .setMessage(R.string.oem_unlocked_device_setup_confirmation)
            .setNegativeButton(R.string.yes_continue) { _, _ -> showOemUnlockedAck() }
            .setPositiveButton(R.string.no_reboot_to_bootloader) { _, _ -> rebootBootloader() }
            .create()
        dialog.show()
        warnNegativeButton(dialog, activity)
    }

    private fun warnNegativeButton(dialog: AlertDialog, activity: Activity) {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(
                activity.resources.getColor(
                    R.color.oem_unlocked_warning_color,
                    activity.theme
                )
            )
    }

    private fun refreshOemUnlockStatus() {
        val manager = getOemLockManager()
        WelcomeData.oemUnlocked.value =
            manager?.isDeviceOemUnlocked ?: true
        WelcomeData.oemUnlockingEnabled.value =
            manager?.isOemUnlockAllowedByUser ?: false
    }

    @MainThread
    private fun showOemUnlockedAck() {
        showPrimaryContent(false)
        background { runOemUnlockedAckTimer() }
    }

    private fun showPrimaryContent(show: Boolean) {
        WelcomeData.displayPrimaryContent.value = show
        WelcomeData.displayOemUnlockedAck.value = !show
    }

    @WorkerThread
    private fun runOemUnlockedAckTimer() {
        for (timer in OEM_UNLOCKED_ACK_TIMER downTo 0) {
            foreground { WelcomeData.oemUnlockedAckTimer.value = timer }
            Thread.sleep(1_000)
        }
    }
}
