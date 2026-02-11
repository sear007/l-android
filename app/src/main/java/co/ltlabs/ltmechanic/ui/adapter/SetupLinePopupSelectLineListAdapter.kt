package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel

private const val TAG = "SelectLineListAdapter";

class SetupLinePopupSelectLineListAdapter constructor(val viewModel: SetupLineViewModel, val machineViewModel: MachineViewModel)
    : RecyclerView.Adapter<SetupLinePopupSelectLineListAdapter.ViewHolder>(), Filterable{

    var data = mutableListOf<MfgLine>()
        set(value) {
            field = value
            dataFull.addAll(value)
            notifyDataSetChanged()
        }

    var dataFull = mutableListOf<MfgLine>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]


        holder.binding.lineTextView.setOnClickListener {
            machineViewModel.setSelectedMfgLine(item)
        }

        val parent = holder.binding.lineContainer

        parent.apply {
            if (position % 2 != 0) {
                setBackgroundColor(Color.WHITE)
                holder.binding.lineTextView.setTextColor(Color.BLACK)
            } else {
                setBackgroundColor(Color.parseColor("#f2f2f2"))
                holder.binding.lineTextView.setTextColor(Color.BLACK)
            }
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemLineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MfgLine) {

            Log.d(TAG, "bind: ${item.mfgLine}")
            binding.line = item.mfgLine

            binding.executePendingBindings()

        }



        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemLineBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun getFilter(): Filter = lineFilter

    private val lineFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList = mutableListOf<MfgLine>()

            Log.d(TAG, "performFiltering: dataFull ${dataFull.size}")

            if (constraint == null || constraint.isEmpty() || constraint == "") {
                filteredList.addAll(dataFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()
                for (line in dataFull) {
                    if (line.mfgLine.toLowerCase().contains(filterPattern)) {
                        filteredList.add(line)
                    }
                }

            }

            val results = FilterResults()
            results.values = filteredList

            return results

        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            data.clear()
            data.addAll(results?.values as Collection<MfgLine>)

//            submitList(data)
            notifyDataSetChanged()

            Log.d(TAG, "publishResults: ${data.size}")

//            viewModel.setMfgLines(data)

            if (data.isEmpty()) {
                viewModel.setEventLineListSearchResultNotFoundToTrue()
            } else {
                viewModel.setEventLineListSearchResultNotFoundToFalse()

            }

        }

    }

    override fun getItemCount(): Int = data.size

}