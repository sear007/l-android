package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemInRepairTicketsBinding
import co.ltlabs.ltmechanic.databinding.ListItemRepairedTicketsBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.InRepairTicket
import co.ltlabs.ltmechanic.domain.RepairedTicket
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicInRepairTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicRepairedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ChecklistViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicRepairedTicketsListAdapter constructor(
    val viewModel: MechanicRepairedTicketsViewModel,
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicRepairedTicketsListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<RepairedTicket>()
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

        parent.setOnClickListener {
            viewModel.setNavigateToTicketPreview(item)
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemRepairedTicketsBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RepairedTicket) {

            binding.ticketNoTextView.text = item.ticketNo
            binding.machineNoTextView.text = item.machineNo
            binding.usernameTextView.text = item.username
            binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
            binding.dateTextView.text = String.format(
                "%s %s",
                DateUtil.formatToDate(item.date),
                DateUtil.formatToTime(item.date)
            )
            binding.locationTextView.text = item.place ?: ""

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemRepairedTicketsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}