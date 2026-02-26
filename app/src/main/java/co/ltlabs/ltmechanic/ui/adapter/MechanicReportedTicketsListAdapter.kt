package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import org.json.JSONObject

class MechanicReportedTicketsListAdapter constructor(
    val viewModel: MechanicReportedTicketsViewModel,
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicReportedTicketsListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<ReportedTicket>()
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


    class ViewHolder private constructor(val binding: ListItemReportedTicketsBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportedTicket) {

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

            val ticketType = TicketType.fromCodeToType(item.status)
            if (ticketType is TicketType.Reopen) {
                binding.statusTextView.setTextColor(Color.parseColor("#FFFFFF"))
                binding.statusTextView.setBackgroundResource(R.drawable.bg_reopen_ticket)
                binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
            } else {
                binding.statusTextView.setTextColor(Color.parseColor("#FF4F3B"))
                binding.statusTextView.setBackgroundResource(0)
            }

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemReportedTicketsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}