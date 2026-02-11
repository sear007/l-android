package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMachineAvailableBinding
import co.ltlabs.ltmechanic.databinding.ListItemMachineHistoryBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.MachineAvailable
import co.ltlabs.ltmechanic.domain.MachineHistory
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicMachineAvailableOtherBrandsListAdapter constructor(
    val languageJsonObject: JSONObject
)
    : RecyclerView.Adapter<MechanicMachineAvailableOtherBrandsListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<MachineAvailable>()
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

        holder.bind(item, languageJsonObject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemMachineAvailableBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MachineAvailable, languageJsonObject: JSONObject) {

            binding.machineNoTextView.text = item.machineNo
            binding.dateTextView.text = DateUtil.formatToDate(item.date)
            binding.statusTextView.text = languageJsonObject.getTranslation(item.status)
            binding.subTypeTextView.text = item.subType
            binding.usernameTextView.text = item.username
            binding.attachmentTextView.text = item.attachment

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMachineAvailableBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}