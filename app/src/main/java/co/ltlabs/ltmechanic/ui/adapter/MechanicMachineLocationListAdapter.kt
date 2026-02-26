package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMachineHistoryBinding
import co.ltlabs.ltmechanic.databinding.ListItemMachineLocationsBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.domain.MachineHistory
import co.ltlabs.ltmechanic.domain.MachineLocation
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsAlternativeMachineLocationsViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import org.json.JSONObject

private const val TAG = "ReportedListAdapter"

class MechanicMachineLocationListAdapter constructor(val viewModel: MechanicReportedTicketsAlternativeMachineLocationsViewModel,
    val languageJsonObject: JSONObject
                                                     )
    : RecyclerView.Adapter<MechanicMachineLocationListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<MachineLocation>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.binding.container.setOnClickListener {
            viewModel.setNavigateToAvailableMachines(item)
        }

        holder.bind(item, languageJsonObject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemMachineLocationsBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MachineLocation, languageJsonObject: JSONObject) {

            binding.locationTextView.text = languageJsonObject.getTranslation(item.location)
            binding.machineCountTextView.text = "(${item.count})"

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMachineLocationsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }


}