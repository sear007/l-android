package co.ltlabs.ltmechanic.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import co.ltlabs.ltmechanic.R
import dagger.android.support.DaggerDialogFragment

abstract class BaseBSDialog<T : ViewDataBinding> : DaggerDialogFragment() {

    private var dismissListener: (() -> Unit)? = null
    private var inflatedView: View? = null
    lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_DialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (inflatedView != null) return inflatedView
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = this
        inflatedView = binding.root
        return inflatedView
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onDestroy() {
        super.onDestroy()
        inflatedView = null
    }

    override fun dismiss() {
        this.dismissAllowingStateLoss()
        super.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissListener?.invoke()
        dismissListener = null
        super.onDismiss(dialog)
    }

    fun onDismissListener(dismissListener: () -> Unit) =
        apply { this.dismissListener = dismissListener }

}