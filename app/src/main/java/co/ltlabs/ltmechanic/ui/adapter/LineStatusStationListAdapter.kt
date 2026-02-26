package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLinesStatusStationsBinding
import co.ltlabs.ltmechanic.databinding.ListItemMachinePlacesBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusStationsViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLinePlacesViewModel
import timber.log.Timber

private const val TAG = "StationListAdapter";

class LineStatusStationListAdapter constructor(val viewModel: LineStatusStationsViewModel)
    : RecyclerView.Adapter<LineStatusStationListAdapter.ViewHolder>(){

    var data = mutableListOf<MachineInStation>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var index = -1

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.binding.lineContainer.setOnClickListener {

            if (!item.station.contains("-A")) {
                val tempData = data.filter { !it.station.contains("-A") }

                val selectedTempData = tempData.filter { it.station == item.station }[0]
                val lastItem = tempData[tempData.size - 1]

                if (selectedTempData.station == lastItem.station) {
                    viewModel.setEndStationToTrue()
                }
            }

            viewModel.setNavigateToStationDetails(item)
//            notifyDataSetChanged()
            Log.d(TAG, "onBindViewHolder: item")
        }

//        if(index==position){
//            holder.binding.containerTextView.setBackgroundColor(Color.parseColor("#2D7AB2"))
//            holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#2D7AB2"))
//        }else{
//            holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1d5072"))
//            holder.binding.containerTextView.setBackgroundColor(Color.parseColor("#1d5072"))
//        }

        if (item.station.contains("-A")) {

            holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1e3c50"))

        } else {

            if(index==position){
                holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#2D7AB2"))
            }else{
                holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1d5072"))
            }


        }
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemLinesStatusStationsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MachineInStation) {

            var machineID = if (item.rfid.count() > 17) "${item.rfid.substring(0, 17)}..." else item.rfid

            binding.placeNoTextView.text = item.station
            binding.machineCodeTextView.text = item.machine
            binding.machineIDTextView.text = machineID
            binding.machineSubTypeTextView.text = item.subType

//            Log.d(TAG, "onBindViewHolder: width ${binding.lineContainer.maxHeight}")




            binding.executePendingBindings()

        }



        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemLinesStatusStationsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


}