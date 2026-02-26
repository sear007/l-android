package co.ltlabs.ltmechanic.ui.changeover.readyco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.ItemReadyAttDesBinding
import co.ltlabs.ltmechanic.domain.changeover.CheckListItem

class ReadyCheckListAdapter(
    private val status: COStatusType,
    val list: List<CheckListItem?>
) : RecyclerView.Adapter<ReadyCheckListAdapter.AttachmentsViewHolder>() {

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
                list[absoluteAdapterPosition]?.isChecked = binding.ivCheck.isChecked
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindItem(data: CheckListItem?, pos: Int) {
            data ?: return
            binding.tvValue.text = String.format("%d. %s", (pos + 1), data.task)
            binding.ivCheck.isChecked = data.isChecked
            if (data.isChecked) {
                if (status == COStatusType.Ready) binding.ivCheck.setReady()
                else binding.ivCheck.setChecked()
            } else binding.ivCheck.setUnChecked()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemReadyAttDesBinding.inflate(layoutInflater, parent, false)
        return AttachmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        holder.bindItem(list[position], position)
    }

    override fun getItemCount() = list.size

}
