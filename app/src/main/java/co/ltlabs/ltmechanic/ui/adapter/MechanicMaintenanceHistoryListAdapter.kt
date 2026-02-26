package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMaintenanceHistoryBinding
import co.ltlabs.ltmechanic.domain.*
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceHistoryViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicMaintenanceHistoryListAdapter constructor(
    val viewModel: MaintenanceHistoryViewModel,
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicMaintenanceHistoryListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<Ticket>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        val parent = holder.binding.reportedTicketsContainer

        holder.binding.linkViewChecklist.setOnClickListener {
            viewModel.setNavigateToChecklist(item)
        }

        if (position % 2 != 0) {
            parent.setBackgroundColor(Color.parseColor("#1D5072"))
        } else {
            parent.setBackgroundColor(Color.parseColor("#1E3C50"))
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemMaintenanceHistoryBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Ticket) {

            binding.linkViewChecklist.text = languageJsonObject.getTranslation(binding.linkViewChecklist.text.toString())
            binding.ticketNoTextView.text = item.ticketNo
            binding.remarksTextView.text = if (item.remarks.isNotBlank()) {
                item.remarks
            } else {
                "-"
            }
            binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
//            binding.dateTextView.text = item.closedDt
//            binding.usernameTextView.text = item.closedBy ?: ""

            binding.dateTextView.text = when (item.status) {
                "SCHEDULED" -> DateUtil.formatToDate(item.createdDt)
                "IN PROGRESS" -> DateUtil.formatToDate(item.grabbedDt)
                else -> DateUtil.formatToDate(item.closedDt)
            }

            binding.usernameTextView.text = when (item.status) {
                "SCHEDULED" -> {
                    item.createdBy
                }

                "IN PROGRESS" -> {
                    item.grabbedBy
                }

                else -> {
                    item.closedBy
                }
            }

            when (item.status) {

                "REPORTED" -> {
                    binding.statusTextView.setTextColor(Color.parseColor("#FB460E"))
                }

                "IN-REPAIR", "IN REPAIR", "IN-PROGRESS", "IN PROGRESS" -> {
                    binding.statusTextView.setTextColor(Color.parseColor("#F59A23"))
                }

                else -> {
                    binding.statusTextView.setTextColor(Color.parseColor("#95F204"))
                }

            }

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMaintenanceHistoryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}