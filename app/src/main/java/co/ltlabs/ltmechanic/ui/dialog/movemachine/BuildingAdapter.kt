package co.ltlabs.ltmechanic.ui.dialog.movemachine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemBuildingDestinationBinding
import co.ltlabs.ltmechanic.domain.BuildingItem
import javax.inject.Inject

class BuildingAdapter @Inject constructor() :
    ListAdapter<BuildingItem, BuildingAdapter.ViewHolder>(COMPARATOR) {

    private var itemClicked: ((pos: Int) -> Unit)? = null

    fun setOnItemClicked(itemClicked: (pos: Int) -> Unit) = apply {
        this.itemClicked = itemClicked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemBuildingDestinationBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBuildingDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                currentList.mapIndexed { index, buildingItem ->
                    buildingItem.isChecked = index == absoluteAdapterPosition
                }
                itemClicked?.invoke(absoluteAdapterPosition)
                notifyDataSetChanged()
            }
        }

        fun bindItem(data: BuildingItem) {
            binding.apply {
                tvBuildingName.text = data.buildingName
                tvBuildingCode.text = data.buildingCode
                rb.isChecked = data.isChecked
                rb.isClickable = false
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<BuildingItem>() {
            override fun areItemsTheSame(oldItem: BuildingItem, newItem: BuildingItem): Boolean {
                return oldItem.id == newItem.id && oldItem.isChecked == newItem.isChecked
            }

            override fun areContentsTheSame(oldItem: BuildingItem, newItem: BuildingItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}