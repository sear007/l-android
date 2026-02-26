package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemClearLineMachinesBinding
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.ClearLineErrorMachine
import co.ltlabs.ltmechanic.util.Line
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderHomeViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel

private const val TAG = "ListAdapter";
//val clickListener: LineLeaderLineListener
class MechanicClearLineMachineListAdapter constructor()
    : RecyclerView.Adapter<MechanicClearLineMachineListAdapter.ViewHolder>() {

    var index = -1
    var data = mutableListOf<ClearLineErrorMachine>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        val parent = holder.binding.lineContainer

        parent.apply {
            if (position % 2 != 0) {
                setBackgroundColor(Color.WHITE)
                holder.binding.machineStationTextView.setTextColor(Color.BLACK)
            } else {
                setBackgroundColor(Color.parseColor("#f2f2f2"))
                holder.binding.machineStationTextView.setTextColor(Color.BLACK)
            }

        }

//        holder.bind(clickListener, item, position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemClearLineMachinesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClearLineErrorMachine) {
            binding.line = "${item.machine} ${item.station}"
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemClearLineMachinesBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


}