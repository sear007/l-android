package co.ltlabs.ltmechanic.ui.changeover.readyco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemReadyAttDesBinding
import co.ltlabs.ltmechanic.domain.changeover.AttachmentsItem

class ReadyAttDesAdapter(
    private val attachments: List<AttachmentsItem?>
) : RecyclerView.Adapter<ReadyAttDesAdapter.AttachmentsViewHolder>() {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setItemClick(item: (pos: Int) -> Unit) = apply {
        this.itemClick = item
    }

    inner class AttachmentsViewHolder(
        private val binding: ItemReadyAttDesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {

            binding.ivCheck.setOnClickListener {
                binding.ivCheck.setToggleCheck()
                attachments[absoluteAdapterPosition]?.isChecked = binding.ivCheck.isChecked
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindItem(data: AttachmentsItem?, pos: Int) {
            data ?: return
            binding.tvValue.text = String.format("%d. %s", (pos + 1), data.desc1)
            if (data.isChecked) {
                binding.ivCheck.setReady()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemReadyAttDesBinding.inflate(layoutInflater, parent, false)
        return AttachmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        holder.bindItem(attachments[position], position)
    }

    override fun getItemCount() = attachments.size

}
