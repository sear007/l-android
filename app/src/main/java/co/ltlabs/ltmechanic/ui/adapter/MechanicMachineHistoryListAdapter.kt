package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMachineHistoryBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.MachineHistory
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicMachineHistoryListAdapter constructor(
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicMachineHistoryListAdapter.ViewHolder>() {

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


    class ViewHolder private constructor(val binding: ListItemMachineHistoryBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MachineHistory) {

            binding.ticketNoTextView.text = item.ticketNo
            binding.problemTextView.text = item.problem
            binding.solutionTextView.text = item.solution
            binding.remarksTextView.text = item.remarks
            binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
            binding.dateTextView.text = DateUtil.formatToDate(item.date)
            binding.usernameTextView.text = item.username

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMachineHistoryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}