package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.databinding.ItemMaintenanceBinding
import co.ltlabs.ltmechanic.domain.maint.MaintItem
import co.ltlabs.ltmechanic.util.getTranslation
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MaintAdapter(
    private val languageJsonObject: JSONObject,
    private val type: String
) : PagingDataAdapter<MaintItem, MaintAdapter.MaintenanceViewHolder>(COMPARATOR) {

    private var itemClick: ((pos: Int) -> Unit)? = null

    fun setOnItemClick(itemClick: (pos: Int) -> Unit) = apply {
        this.itemClick = itemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMaintenanceBinding.inflate(layoutInflater, parent, false)
        return MaintenanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        holder.bindingItem(getItem(position), position)
    }

    inner class MaintenanceViewHolder(
        private val binding: ItemMaintenanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.item.setOnClickListener {
                itemClick?.invoke(absoluteAdapterPosition)
            }
        }

        fun bindingItem(data: MaintItem?, position: Int) {
            data ?: return
            val formatTitleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatUpdatedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            var updatedDate = "-"
            var updatedUser = "-"
            val status = data.status

            val statusName =
                if (status is MaintType.Wip || status is MaintType.Schedule) {
                    "${MaintType.SCHEDULED} DATE"
                } else {
                    "${MaintType.CLOSED} DATE"
                }
            with(binding) {
                tvMaintDate.text = String.format(
                    "${languageJsonObject.getTranslation(statusName)}: %s (%d)",
                    formatTitleDate.format(getDateByStatus(data)),
                    data.dateCount ?: 0
                )
                tvMachineCode.text = data.machineNo ?: "-"
                tvTicketCode.text = data.ticketNo ?: "-"
                tvLine.text=data.place
                tvStatus.text = if (status is MaintType.Wip && data.grabbedDt != null) {
                    val grabbedDt = data.grabbedDt
                    val nowDt = Date()
                    val result = nowDt.time - grabbedDt.time
                    "WIP ${timerString(result)}"
                } else status?.status
                vColor.setBackgroundColor(Color.parseColor(data.status?.colorCode))
                tvStatus.setTextColor(Color.parseColor(data.status?.colorCode))
                when (status) {
                    is MaintType.Completed -> {
                        updatedDate = formatUpdatedDate.format(data.closedDt ?: Date())
                        updatedUser = data.closedBy ?: "-"
                    }
                    is MaintType.Cancelled -> {
                        updatedDate = formatUpdatedDate.format(data.canceledDt ?: Date())
                        updatedUser = data.canceledBy ?: "-"
                    }
                    is MaintType.Wip -> {
                        updatedDate = formatUpdatedDate.format(data.grabbedDt ?: Date())
                        updatedUser = data.grabbedBy ?: "-"
                    }
                    else -> {
                        updatedDate = "-"
                        updatedUser = "-"
                    }
                }
                tvUpdatedDate.text = updatedDate
                tvUpdatedUsername.text = updatedUser
            }

            if (position > 0) {
                val prePosition = position - 1
                val preItem = getItem(prePosition)
                val curItem = getItem(position)
                if (type == MaintType.CLOSED) {
                    if ((curItem?.status == preItem?.status) && (formatTitleDate.format(
                            getDateByStatus(curItem)
                        ) == formatTitleDate.format(
                            getDateByStatus(preItem)
                        ))
                    ) {
                        isShowing(false)
                    } else {
                        isShowing(true)
                    }
                } else {
                    if ((formatTitleDate.format(getDateByStatus(curItem)) == formatTitleDate.format(
                            getDateByStatus(preItem)
                        ))
                    ) {
                        isShowing(false)
                    } else {
                        isShowing(true)
                    }
                }
            } else {
                isShowing(true)
            }
        }

        private fun isShowing(show: Boolean) {
            if (!show) {
                binding.marginTop = 16.toFloat()
                binding.isShowMaintDate = false
            } else {
                binding.marginTop = 0.toFloat()
                binding.isShowMaintDate = true
            }
        }

        private fun timerString(mill: Long): String {
            val minute = ((mill / (1000 * 60)) % 60)
            val hour = ((mill / (1000 * 60 * 60)) % 24)
            if (hour > 0) {
                return String.format("%dh %dm", hour, minute)
            }
            return String.format("0h %dm", minute)
        }

        private fun getDateByStatus(data: MaintItem?): Date {
            return when (data?.status) {
                is MaintType.Wip, is MaintType.Schedule -> data.reportedDt ?: Date()
                is MaintType.Cancelled -> data.canceledDt ?: Date()
                else -> data?.closedDt ?: Date()
            }
        }
    }

    companion object {

        val COMPARATOR = object : DiffUtil.ItemCallback<MaintItem>() {
            override fun areItemsTheSame(oldItem: MaintItem, newItem: MaintItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MaintItem, newItem: MaintItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}