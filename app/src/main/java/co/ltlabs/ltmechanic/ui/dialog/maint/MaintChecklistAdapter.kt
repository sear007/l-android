package co.ltlabs.ltmechanic.ui.dialog.maint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemAreaDestinationBinding
import co.ltlabs.ltmechanic.databinding.ItemBuildingDestinationBinding
import co.ltlabs.ltmechanic.databinding.ItemMaintChecklistBinding
import co.ltlabs.ltmechanic.domain.AreaItem
import co.ltlabs.ltmechanic.domain.BuildingItem
import co.ltlabs.ltmechanic.domain.MaintenanceChecklist
import co.ltlabs.ltmechanic.domain.maintenance.ChecklistItem
import javax.inject.Inject

class MaintChecklistAdapter @Inject constructor() :
    ListAdapter<ChecklistItem, MaintChecklistAdapter.ViewHolder>(COMPARATOR) {

    private var itemClicked: ((pos: Int) -> Unit)? = null

    fun setOnItemClicked(itemClicked: (pos: Int) -> Unit) = apply {
        this.itemClicked = itemClicked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemMaintChecklistBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMaintChecklistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                currentList.mapIndexed { index, item ->
                    item.checked = index == absoluteAdapterPosition
                }
                itemClicked?.invoke(absoluteAdapterPosition)
                notifyDataSetChanged()
            }
        }

        fun bindItem(data: ChecklistItem) {
            binding.apply {
                tvName.text = data.name
                tvCode.text = data.checklistType
                rb.isChecked = data.checked
                rb.isClickable = false
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ChecklistItem>() {
            override fun areItemsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChecklistItem, newItem: ChecklistItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}