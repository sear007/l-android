package co.ltlabs.ltmechanic.ui.changeover.detailco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemOperatorBinding
import co.ltlabs.ltmechanic.domain.changeover.OperatorItem
import org.json.JSONObject
import javax.inject.Inject

class OperatorAdapter @Inject constructor(
    private val languageJsonObject: JSONObject
) :
    ListAdapter<OperatorItem, OperatorAdapter.OperatorViewHold>(COMPARATOR) {

    private var itemClick: ((parentPos: Int, childPos: Int) -> Unit)? = null

    fun setOnItemClick(itemClick: (parentPos: Int, childPos: Int) -> Unit) = apply {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorViewHold {
        val inflateLayout = LayoutInflater.from(parent.context)
        val binding = ItemOperatorBinding.inflate(inflateLayout, parent, false)
        return OperatorViewHold(binding)
    }

    override fun onBindViewHolder(holder: OperatorViewHold, position: Int) {
        holder.binding(getItem(position))
    }

    inner class OperatorViewHold(
        private val binding: ItemOperatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var operationAdapter: OperationAdapter

        fun binding(data: OperatorItem) {
            binding.tvOrder.text = data.station?.toString()
            operationAdapter = OperationAdapter(languageJsonObject)
            binding.rvOperation.adapter = operationAdapter
            operationAdapter.submitList(data.operations)

            operationAdapter.setOnItemClick {
                itemClick?.invoke(absoluteAdapterPosition, it)
            }
        }
    }

    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<OperatorItem>() {
            override fun areItemsTheSame(oldItem: OperatorItem, newItem: OperatorItem): Boolean {
                return oldItem.operations == newItem.operations && oldItem.station == newItem.station
            }

            override fun areContentsTheSame(oldItem: OperatorItem, newItem: OperatorItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}