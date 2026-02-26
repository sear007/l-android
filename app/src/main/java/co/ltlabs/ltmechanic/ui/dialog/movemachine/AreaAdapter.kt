package co.ltlabs.ltmechanic.ui.dialog.movemachine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemAreaDestinationBinding
import co.ltlabs.ltmechanic.domain.Areas
import javax.inject.Inject

class AreaAdapter @Inject constructor() :
    ListAdapter<Areas, AreaAdapter.ViewHolder>(COMPARATOR) {

    private var itemClicked: ((pos: Int) -> Unit)? = null

    fun setOnItemClicked(itemClicked: (pos: Int) -> Unit) = apply {
        this.itemClicked = itemClicked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemAreaDestinationBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAreaDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                currentList.mapIndexed { index, buildingItem ->
                    buildingItem.isSelected = index == absoluteAdapterPosition
                }
                itemClicked?.invoke(absoluteAdapterPosition)
                notifyDataSetChanged()
            }
        }

        fun bindItem(data: Areas) {
            binding.apply {
                tvAreaName.text = data.name
                tvAreaCode.text = data.area
                rb.isChecked = data.isSelected
                rb.isClickable = false
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Areas>() {
            override fun areItemsTheSame(oldItem: Areas, newItem: Areas): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Areas, newItem: Areas): Boolean {
                return oldItem == newItem
            }
        }
    }
}