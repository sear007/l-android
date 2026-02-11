package co.ltlabs.ltmechanic.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMachinePlacesBinding
import co.ltlabs.ltmechanic.domain.MachineInStation
import javax.inject.Inject


class SetupLineMachinePlacesListAdapter @Inject constructor() :
    ListAdapter<MachineInStation, SetupLineMachinePlacesListAdapter.ViewHolder>(DIFFER) {

    private var index = -1

    private var itemClicked: ((pos: Int) -> Unit)? = null

    fun setOnItemClickListener(itemClicked: (pos: Int) -> Unit) =
        apply { this.itemClicked = itemClicked }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemMachinePlacesBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(val binding: ListItemMachinePlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            index = -1
            binding.root.setOnClickListener {
                index = absoluteAdapterPosition
                itemClicked?.invoke(absoluteAdapterPosition)
            }
        }

        fun bind(position: Int) {
            val item = getItem(position)
            val machineID =
                if (item.rfid.count() > 17) "${item.rfid.substring(0, 17)}..." else item.rfid

            binding.placeNoTextView.text = item.station
            binding.machineCodeTextView.text = item.machine
            binding.machineIDTextView.text = machineID
            binding.machineSubTypeTextView.text = item.subType
            binding.executePendingBindings()

            if (index == position) {
                binding.containerTextView.setBackgroundColor(Color.parseColor("#2D7AB2"))
                binding.lineContainer.setBackgroundColor(Color.parseColor("#2D7AB2"))
            } else {
                binding.lineContainer.setBackgroundColor(Color.parseColor("#1d5072"))
                binding.containerTextView.setBackgroundColor(Color.parseColor("#1d5072"))
            }

            if (item.station.contains("-A")) {
                binding.lineContainer.setBackgroundColor(Color.parseColor("#1e3c50"))
            } else {
                if (index == position) {
                    binding.lineContainer.setBackgroundColor(Color.parseColor("#2D7AB2"))
                } else {
                    binding.lineContainer.setBackgroundColor(Color.parseColor("#1d5072"))
                }
            }
        }
    }

    companion object {
        private val DIFFER = object : DiffUtil.ItemCallback<MachineInStation>() {

            override fun areItemsTheSame(
                oldItem: MachineInStation,
                newItem: MachineInStation
            ): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: MachineInStation,
                newItem: MachineInStation
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}