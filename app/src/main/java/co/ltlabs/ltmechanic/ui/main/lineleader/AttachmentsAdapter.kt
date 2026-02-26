package co.ltlabs.ltmechanic.ui.main.lineleader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemAttachmentBinding
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.util.FILE_API_ADDED_URL
import com.bumptech.glide.RequestManager

class AttachmentsAdapter(
    private val requestManager: RequestManager,
    private val attachments: List<String>
) : RecyclerView.Adapter<AttachmentsAdapter.AttachmentsViewHolder>() {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setItemClick(item: (pos: Int) -> Unit) = apply {
        this.itemClick = item
    }

    inner class AttachmentsViewHolder(
        private val binding: ItemAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindItem(data: String) {
            with(binding) {
                requestManager.load(data)
                    .into(ivAttachment)
                ivPlayButton.isVisible = data.contains(".mp4")
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemAttachmentBinding.inflate(layoutInflater, parent, false)
        return AttachmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        holder.bindItem(attachments[position])
    }

    override fun getItemCount() = attachments.size

}