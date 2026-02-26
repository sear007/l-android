package co.ltlabs.ltmechanic.ui.main.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.TextViewOnlyBinding
import co.ltlabs.ltmechanic.domain.Areas
import java.util.*
import javax.inject.Inject

class AreasAdapter @Inject constructor() :
    ListAdapter<Areas, AreasAdapter.AreasViewHolder>(COMPARATOR) {

    private var itemClick: (() -> Unit)? = null
    private var unfilteredList: List<Areas> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreasViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = TextViewOnlyBinding.inflate(inflate, parent, false)
        return AreasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AreasViewHolder, position: Int) {
        holder.bindItem(position)
    }

    fun modifyList(list: List<Areas>?) {
        list ?: return
        unfilteredList = list
        submitList(list)
    }

    fun filter(query: CharSequence?, callback: (list: List<Areas>) -> Unit) {
        val list = mutableListOf<Areas>()

        if (!query.isNullOrEmpty()) {
            list.addAll(unfilteredList.filter {
                it.name?.lowercase(Locale.getDefault())
                    ?.contains(query.toString().lowercase(Locale.getDefault())) == true
            })
        } else {
            list.addAll(unfilteredList)
        }

        submitList(list)
        callback.invoke(list)
    }

    fun setOnItemClick(itemClick: (() -> Unit)) = apply {
        this.itemClick = itemClick
    }

    inner class AreasViewHolder(private val binding: TextViewOnlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(absoluteAdapterPosition)
                item.isSelected = !item.isSelected
                setSelected(item)
                itemClick?.invoke()
            }
        }

        fun bindItem(position: Int) {
            val data = getItem(position) ?: return
            binding.tvValue.text = data.name
            setSelected(data)
        }

        private fun setSelected(data: Areas) {
            binding.tvValue.isSelected = data.isSelected
            binding.tvValue.isActivated =
                !binding.tvValue.isSelected && (absoluteAdapterPosition % 2) == 0
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