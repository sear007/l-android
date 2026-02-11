package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMachineHistoryBinding
import co.ltlabs.ltmechanic.databinding.ListItemRepairHistoryBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.MachineHistory
import co.ltlabs.ltmechanic.domain.RepairHistory
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicRepairHistoryListAdapter constructor(
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicRepairHistoryListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<MachineHistory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        val parent = holder.binding.reportedTicketsContainer

        if (position % 2 != 0) {
            parent.setBackgroundColor(Color.parseColor("#1D5072"))
        } else {
            parent.setBackgroundColor(Color.parseColor("#1E3C50"))
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemRepairHistoryBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MachineHistory) {

            binding.ticketNoTextView.text = item.ticketNo
            binding.problemTextView.text = item.problem
            binding.solutionTextView.text = if (item.solution.isNotBlank()) {
                item.solution
            } else {
                "-"
            }
            binding.remarksTextView.text = if (item.remarks.isNotBlank()) {
                item.remarks
            } else {
                "-"
            }
            binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
            binding.dateTextView.text = DateUtil.formatToDate(item.date)

            binding.usernameTextView.text = item.username

            when (item.status) {

                "REPORTED" -> {
                    binding.statusTextView.setTextColor(Color.parseColor("#FB460E"))
                }

                "IN-REPAIR", "IN REPAIR" -> {
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
                val binding = ListItemRepairHistoryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}