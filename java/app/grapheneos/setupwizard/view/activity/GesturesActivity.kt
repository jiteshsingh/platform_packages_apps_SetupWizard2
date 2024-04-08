package app.grapheneos.setupwizard.view.activity

import android.content.Intent
import android.util.Log
import android.view.View
import app.grapheneos.setupwizard.R
import app.grapheneos.setupwizard.action.GesturesActions
import app.grapheneos.setupwizard.action.SetupWizard

class GesturesActivity : SetupWizardActivity(
    R.layout.activity_gestures,
    R.drawable.baseline_gesture_glif,
    R.string.swipe_gestures_title,
    R.string.swipe_gestures_desc
) {
    companion object {
        private const val TAG = "GesturesActivity"
    }

    override fun bindViews() {
        primaryButton.setText(this, R.string.try_it)
    }

    override fun setupActions() {
        secondaryButton.setOnClickListener { SetupWizard.next(this) }
        primaryButton.setOnClickListener { GesturesActions.launchTutorial(this) }
    }

    override fun onActivityResult(resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: $resultCode, data=$data")
        GesturesActions.handleResult(this, resultCode)
        super.onActivityResult(resultCode, data)
    }
}
