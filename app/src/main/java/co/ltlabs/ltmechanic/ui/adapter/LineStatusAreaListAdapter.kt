package co.ltlabs.ltmechanic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineStatusAreasBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusViewModel

class LineStatusAreaListAdapter constructor(val viewModel: LineStatusViewModel)
    : ListAdapter<MfgLine, LineStatusAreaListAdapter.ViewHolder>(MfgLineDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.sewingLineTextView.setOnClickListener {
            viewModel.setNavigateToStations(item)
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemLineStatusAreasBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MfgLine) {

            binding.sewingLineTextView.text = item.mfgLine

            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemLineStatusAreasBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

}

class MfgLineDiffCallback : DiffUtil.ItemCallback<MfgLine>() {
    override fun areItemsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
        return oldItem.mfgLineId == newItem.mfgLineId
    }

    override fun areContentsTheSame(oldItem: MfgLine, newItem: MfgLine): Boolean {
        return oldItem == newItem
    }
}