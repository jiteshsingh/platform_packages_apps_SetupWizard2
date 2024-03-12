package app.grapheneos.setupwizard.data

import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.grapheneos.setupwizard.OEM_UNLOCKED_ACK_TIMER
import app.grapheneos.setupwizard.action.WelcomeActions
import app.grapheneos.setupwizard.appContext
import java.util.Locale

object WelcomeData : ViewModel() {
    val selectedLanguage = MutableLiveData<Locale>()
    val oemUnlocked = MutableLiveData<Boolean>() // bootloader unlocked
    val oemUnlockingEnabled = MutableLiveData<Boolean>() // bootloader unlocking is allowed
    val displayPrimaryContent = MutableLiveData<Boolean>()
    val displayOemUnlockedAck = MutableLiveData<Boolean>()
    val oemUnlockedAckTimer = MutableLiveData(OEM_UNLOCKED_ACK_TIMER)

    val hasTelephonyFeature: Boolean by lazy {
        appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CALLING)
    }

    init {
        WelcomeActions
    }
}
