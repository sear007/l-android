package co.ltlabs.ltmechanic.ui.changeover

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.DialogViewAttachmentBinding
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.FILE_API_ADDED_URL
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject


class ViewAttachmentBSDialog : DaggerDialogFragment() {

    @Inject
    lateinit var requestManager: RequestManager

    private var item: AttachmentsItem? = null
    private lateinit var binding: DialogViewAttachmentBinding
    private var dismissListener: (() -> Unit)? = null

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels - 70
            val height = displayMetrics.heightPixels - 150
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_DialogStyle)
        arguments?.let {
            item = it.getParcelable(EXTRA_ATTACHMENT)
        }
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
    ): View {
        binding = DialogViewAttachmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvClose.setOnClickListener {
                dismiss()
            }

            requestManager.load(fullAttachmentImage(item?.imgLink))
                .into(ivPre)

            tvTitle.text = item?.desc1
        }
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


    companion object {
        val BASE_IMAGE_URL = "${AppModule.scheme}://${AppModule.host}$FILE_API_ADDED_URL/api/files/by-name?path="

        fun fullAttachmentImage(path: String?): String {
            return "$BASE_IMAGE_URL$path&accessToken=${AuthUtil.token}&unlinked"
        }

        private const val EXTRA_ATTACHMENT = "EXTRA_ATTACHMENT"
        fun newInstance(item: AttachmentsItem) = ViewAttachmentBSDialog().apply {
            arguments = bundleOf(EXTRA_ATTACHMENT to item)
        }
    }
}