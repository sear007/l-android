package co.ltlabs.ltmechanic.ui.changeover.prepareco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemPrepareAttDesBinding
import co.ltlabs.ltmechanic.domain.changeover.CheckListItem

class PrepareCheckListAdapter(
    private val attachments: List<CheckListItem?>
) : RecyclerView.Adapter<PrepareCheckListAdapter.MyViewHolder>() {

    inner class MyViewHolder(
        private val binding: ItemPrepareAttDesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(data: CheckListItem?, pos: Int) {
            data ?: return
            binding.tvAttachment.text = String.format("%d. %s", (pos + 1), data.description)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemPrepareAttDesBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItem(attachments[position], position)
    }

    override fun getItemCount() = attachments.size

}
