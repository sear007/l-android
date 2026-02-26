package co.ltlabs.ltmechanic.ui.changeover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemAttImageBinding
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.FILE_API_ADDED_URL
import com.bumptech.glide.RequestManager

class AttImageAdapter(
    private val requestManager: RequestManager,
    val attachments: List<AttachmentsItem?>
) : RecyclerView.Adapter<AttImageAdapter.AttachmentsViewHolder>() {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setItemClick(item: (pos: Int) -> Unit) = apply {
        this.itemClick = item
    }

    inner class AttachmentsViewHolder(
        private val binding: ItemAttImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindItem(data: AttachmentsItem?) {
            data ?: return
            requestManager.load(ViewAttachmentBSDialog.fullAttachmentImage(data.imgLink))
                .into(binding.ivAttachment)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemAttImageBinding.inflate(layoutInflater, parent, false)
        return AttachmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        holder.bindItem(attachments[position])
    }

    override fun getItemCount() = attachments.size

}
