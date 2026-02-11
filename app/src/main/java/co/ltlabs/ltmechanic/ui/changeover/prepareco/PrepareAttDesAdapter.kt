package co.ltlabs.ltmechanic.ui.changeover.prepareco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemPrepareAttDesBinding
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem

class PrepareAttDesAdapter(
    private val attachments: List<AttachmentsItem?>
) : RecyclerView.Adapter<PrepareAttDesAdapter.AttachmentsViewHolder>() {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setItemClick(item: (pos: Int) -> Unit) = apply {
        this.itemClick = item
    }

    inner class AttachmentsViewHolder(
        private val binding: ItemPrepareAttDesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindItem(data: AttachmentsItem?, pos: Int) {
            data ?: return
            binding.tvAttachment.text = String.format("%d. %s", (pos + 1), data.desc1)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemPrepareAttDesBinding.inflate(layoutInflater, parent, false)
        return AttachmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        holder.bindItem(attachments[position], position)
    }

    override fun getItemCount() = attachments.size

}
