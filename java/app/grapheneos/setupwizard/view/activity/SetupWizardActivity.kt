package app.grapheneos.setupwizard.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import app.grapheneos.setupwizard.R
import com.google.android.setupcompat.template.FooterBarMixin
import com.google.android.setupcompat.template.FooterButton
import com.google.android.setupdesign.GlifLayout
import com.google.android.setupdesign.R as SudR
import com.google.android.setupdesign.util.ThemeHelper

/**
 * This is the base activity for all setup wizard activities.
 */
abstract class SetupWizardActivity(
    @LayoutRes protected val layoutResID: Int? = null,
    @DrawableRes protected val icon: Int? = null,
    @StringRes protected val header: Int? = null,
    @StringRes protected val description: Int? = null,
) : AppCompatActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var footerBarMixin: FooterBarMixin
    protected val primaryButton: FooterButton by lazy {
        val button = FooterButton.Builder(this)
            .setButtonType(FooterButton.ButtonType.NEXT)
            .setTheme(SudR.style.SudGlifButton_Primary)
            .setText(R.string.next)
            .build()
        footerBarMixin.primaryButton = button
        button
    }
    protected val secondaryButton: FooterButton by lazy {
        val button = FooterButton.Builder(this)
            .setButtonType(FooterButton.ButtonType.SKIP)
            .setTheme(SudR.style.SudGlifButton_Secondary)
            .setText(R.string.skip)
            .build()
        footerBarMixin.secondaryButton = button
        button
    }

    protected fun superOnCreateAtBaseClass(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        superOnCreateAtBaseClass(savedInstanceState)
        activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            onActivityResult(result.resultCode, result.data)
        }
        setTheme(ThemeHelper.getSuwDefaultTheme(applicationContext))
        ThemeHelper.trySetDynamicColor(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (layoutResID == null) return
        // setup view
        setContentView(layoutResID)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.updatePadding(
                top = insets.getInsets(WindowInsets.Type.systemBars()).top,
                bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            )
            insets
        }
        initBaseView()
        bindViews()
        setupActions()
    }

    private fun initBaseView() {
        val glifLayout = findViewById<GlifLayout>(R.id.glif_layout) ?: return
        footerBarMixin = glifLayout.getMixin(FooterBarMixin::class.java)
        if (icon != null) glifLayout.icon = getDrawable(icon)
        if (header != null) glifLayout.setHeaderText(header)
        if (description != null) glifLayout.setDescriptionText(description)
    }

    @MainThread
    abstract fun bindViews()

    @MainThread
    abstract fun setupActions()

    protected open fun onActivityResult(resultCode: Int, data: Intent?) {}

    fun startActivityForResult(intent: Intent) {
        activityResultLauncher.launch(intent)
    }
}
