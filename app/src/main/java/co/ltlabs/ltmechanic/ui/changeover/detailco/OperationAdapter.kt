package co.ltlabs.ltmechanic.ui.changeover.detailco

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.ItemOperationBinding
import co.ltlabs.ltmechanic.domain.changeover.OperationItem
import co.ltlabs.ltmechanic.util.getTranslation
import org.json.JSONObject

class OperationAdapter(
    private val jTranslate: JSONObject
) :
    ListAdapter<OperationItem, OperationAdapter.OperationViewHold>(COMPARATOR) {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setOnItemClick(itemClick: (pos: Int) -> Unit) = apply {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OperationAdapter.OperationViewHold {
        val inflateLayout = LayoutInflater.from(parent.context)
        val binding = ItemOperationBinding.inflate(inflateLayout, parent, false)
        return OperationViewHold(binding)
    }

    override fun onBindViewHolder(holder: OperationViewHold, position: Int) {
        holder.binding(getItem(position))
    }

    inner class OperationViewHold(
        private val binding: ItemOperationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun binding(data: OperationItem) {
            binding.apply {
                tvMacSubType.text = data.macSubType
                tvMachineNo.text = data.machine ?: "-"
                tvDescription.text = data.operation
                tvNeedleType.text = data.needleType
                ivCritical.setImageResource(data.isCritical?.imageRes ?: R.drawable.ic_un_critical)
                val status = data.status ?: COStatusType.New
                tvStatus.text = jTranslate.getTranslation(data.status?.status ?: "")
                tvStatus.setTextColor(Color.parseColor(status.colorCode))
                vColor.setBackgroundColor(Color.parseColor(status.colorCode))

                val iv = if (data.isExist) View.INVISIBLE else View.VISIBLE
                ivCritical.visibility = iv
                tvDescription.visibility = iv
                tvMacSubType.visibility = iv
                tvMachineNo.visibility = iv
                tvNeedleType.visibility = iv
                tvStatus.visibility = iv
                binding.marginTop =
                    if (!data.isExist) {
                        8.toFloat()
                    } else 0.toFloat()
            }
        }
    }

    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<OperationItem>() {
            override fun areItemsTheSame(oldItem: OperationItem, newItem: OperationItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: OperationItem,
                newItem: OperationItem
            ): Boolean {
                return oldItem == newItem
            }
        }

    }
}