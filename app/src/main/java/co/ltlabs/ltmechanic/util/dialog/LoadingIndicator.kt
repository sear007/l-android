package co.ltlabs.ltmechanic.util.dialog

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.view.Window
import co.ltlabs.ltmechanic.R
import timber.log.Timber

class LoadingIndicator {
    private var dialog: Dialog? = null

    private var dismissCallback: ((event: String?) -> Unit)? = null

    fun onDismissListener(callback: (event: String?) -> Unit) = apply {
        this.dismissCallback = callback
    }

    fun show(context: Context, isCancellable: Boolean = false, event: String? = null) {
        if (dialog != null && dialog!!.isShowing) {
            return
        }
        dialog = Dialog(context).apply {
            window?.requestFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(R.color.colorTransparent)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setContentView(R.layout.loading_indicator)
            setCancelable(isCancellable)
            setCanceledOnTouchOutside(isCancellable)
            show()

            setOnDismissListener {
                dismissCallback?.invoke(event)
                dialog = null
            }
        }
    }

    fun dismiss() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    companion object {
        const val CREATE_TICKET = "CREATE_TICKET"
        const val UPLOAD_FILE = "UPLOAD_FILE"

        const val SUBMIT_CHECKLIST = "SUBMIT_CHECKLIST"
        const val GET_SOLUTION = "GET_SOLUTION"
    }
}