package co.ltlabs.ltmechanic.ui.main.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.TextViewOnlyBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import java.util.*
import javax.inject.Inject

class LinesAdapter @Inject constructor() :
    ListAdapter<MfgLine, LinesAdapter.LinesViewHolder>(COMPARATOR) {

    private var itemClick: (() -> Unit)? = null
    private var unfilteredList: List<MfgLine> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinesViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = TextViewOnlyBinding.inflate(inflate, parent, false)
        return LinesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LinesViewHolder, position: Int) {
        holder.bindItem(position)
    }

    fun setOnItemClick(itemClick: (() -> Unit)) = apply {
        this.itemClick = itemClick
    }

    fun modifyList(list: List<MfgLine>?) {
        list ?: return
        unfilteredList = list
        submitList(list)
    }

    fun filter(query: CharSequence?, callback: (list: List<MfgLine>) -> Unit) {
        val list = mutableListOf<MfgLine>()

        if (!query.isNullOrEmpty()) {
            list.addAll(unfilteredList.filter {
                it.mfgLine.lowercase(Locale.getDefault())
                    .contains(
                        query.toString().lowercase(Locale.getDefault())
                    )
            })
        } else {
            list.addAll(unfilteredList)
        }

        submitList(list)
        callback.invoke(list)
    }

    inner class LinesViewHolder(private val binding: TextViewOnlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(absoluteAdapterPosition)
                item.checked = item.checked == false
                setSelected(item)
                itemClick?.invoke()
            }
        }

        fun bindItem(position: Int) {
            val data = getItem(position) ?: return
            binding.tvValue.text = data.mfgLine
            setSelected(data)
        }

        private fun setSelected(data: MfgLine) {
            binding.tvValue.isSelected = data.checked == true
            binding.tvValue.isActivated = !binding.tvValue.isSelected  && (absoluteAdapterPosition % 2) == 0
        }
    }

    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<MfgLine>() {
            override fun areItemsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
                return oldItem.mfgLineId == newItem.mfgLineId
            }

            override fun areContentsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
                return oldItem == newItem
            }
        }
    }
}