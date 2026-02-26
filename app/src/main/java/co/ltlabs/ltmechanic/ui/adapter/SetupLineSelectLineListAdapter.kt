package co.ltlabs.ltmechanic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.databinding.ListItemMachinePlacesBinding
import co.ltlabs.ltmechanic.databinding.ListItemSetupLinesBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineSelectLineViewModel

class SetupLineSelectLineListAdapter constructor(val viewModel: SetupLineSelectLineViewModel)
    : ListAdapter<MfgLine, SetupLineSelectLineListAdapter.ViewHolder>(MfgLineDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.sewingLineTextView.setOnClickListener {
            viewModel.displaySetupLine(item)
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemSetupLinesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MfgLine) {

            binding.sewingLineTextView.text = item.mfgLine

            binding.executePendingBindings()

        }



        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSetupLinesBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

}

//class MfgLineDiffCallback : DiffUtil.ItemCallback<MfgLine>() {
//    override fun areItemsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
//        return oldItem == newItem
//    }
//}