package co.ltlabs.ltmechanic.ui.changeover

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.ItemChangeOverBinding
import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.util.getTranslation
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class COAdapter @Inject constructor(
    private val languageJsonObject: JSONObject
) : PagingDataAdapter<COItem, COAdapter.ChangeOverViewHolder>(COMPARATOR) {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setOnItemClick(itemClick: (pos: Int) -> Unit) = apply {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeOverViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemChangeOverBinding.inflate(layoutInflater, parent, false)
        return ChangeOverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChangeOverViewHolder, position: Int) {
        holder.bindingItem(getItem(position), position)
    }

    inner class ChangeOverViewHolder(
        private val binding: ItemChangeOverBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.item.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindingItem(data: COItem?, position: Int) {
            data ?: return
            val formatCODate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatUpdatedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            with(binding) {
                tvCoDate.text = String.format(
                    "CO DATE: %s (%d)",
                    formatCODate.format(data.coRequestDate ?: Date()),
                    data.dateCount ?: 0
                )
                tvUpdatedDate.text = if (data.status is COStatusType.New) "-"
                            else formatUpdatedDate.format(data.updatedDate ?: Date())

                tvCo.text = data.coRequestNo
                tvType.text = data.type ?: "-"
                tvStyle.text = data.style
                tvLine.text = data.mfgLine
                tvStatus.text = languageJsonObject.getTranslation(data.status?.status ?: "")
                tvMachine.text = String.format("M/C: %d", data.mcQty)
                tvMachineCritical.text = data.criticalMcQty?.toString()

                vColor.setBackgroundColor(Color.parseColor(data.status?.colorCode))
                tvStatus.setTextColor(Color.parseColor(data.status?.colorCode))
            }

            // Check to show or hide tvCoDate
            if (position > 0) {
                val prePosition = position - 1
                val preItem = getItem(prePosition)
                val curItem = getItem(position)
                if (curItem?.coRequestDate == preItem?.coRequestDate) {
                    binding.marginTop = 16.toFloat()
                    binding.isShowCODate = false
                } else {
                    binding.marginTop = 0.toFloat()
                    binding.isShowCODate = true
                }
            } else {
                binding.isShowCODate = true
                binding.marginTop = 0.toFloat()
            }
        }
    }

    companion object {

        val COMPARATOR = object : DiffUtil.ItemCallback<COItem>() {
            override fun areItemsTheSame(oldItem: COItem, newItem: COItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: COItem, newItem: COItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}