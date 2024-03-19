package app.grapheneos.setupwizard

import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.google.android.setupcompat.template.FooterBarMixin
import com.google.android.setupcompat.template.FooterButton

fun FooterButton.setText(@StringRes resId: Int) {
    setText(appContext, resId)
}
