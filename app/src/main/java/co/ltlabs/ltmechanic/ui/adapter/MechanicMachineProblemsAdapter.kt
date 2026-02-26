package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.databinding.ListItemProblemBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.domain.Problem
import co.ltlabs.ltmechanic.util.Line
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.CreateTicketViewModel
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderHomeViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicCreateTicketViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel

private const val TAG = "ListAdapter";
//val clickListener: LineLeaderLineListener
class MechanicMachineProblemsAdapter constructor(val viewModel: MechanicCreateTicketViewModel, val ticketViewModel: TicketViewModel)
    : RecyclerView.Adapter<MechanicMachineProblemsAdapter.ViewHolder>(), Filterable {

    var index = -1
    var data = mutableListOf<Problem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dataFull = listOf<Problem>()

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        var checked = item.checked ?: false


        val parent = holder.binding.lineContainer

        parent.apply {

            setOnClickListener {
                checked = !checked
                data[position].checked = checked
                ticketViewModel.updateProblem(data)
                ticketViewModel.selectedProblemTemp = data[position]
//                viewModel.tempEventLinesChanged = true
//                notifyItemChanged(holder.adapterPosition)
                index = position
                notifyDataSetChanged()

            }

            if (index == position) {
                setBackgroundColor(Color.parseColor("#1d547a"))
                holder.binding.problemTextView.setTextColor(Color.WHITE)
            } else {
                if (position % 2 != 0) {
                    setBackgroundColor(Color.WHITE)
                    holder.binding.problemTextView.setTextColor(Color.BLACK)
                } else {
                    setBackgroundColor(Color.parseColor("#f2f2f2"))
                    holder.binding.problemTextView.setTextColor(Color.BLACK)
                }
            }
        }



//        holder.bind(clickListener, item, position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun getFilter(): Filter = lineFilter

    private val lineFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList = mutableListOf<Problem>()
            Log.d(TAG, "performFiltering: dataFull size: ${dataFull.size}")

            if (constraint == null || constraint.isEmpty() || constraint == "") {
                filteredList.addAll(dataFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()
                for (problem in dataFull) {
                    if (problem.desc1.toLowerCase().contains(filterPattern)) {
                        filteredList.add(problem)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList

            return results

        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            data.clear()
            data.addAll(results?.values as Collection<Problem>)

            notifyDataSetChanged()

            Log.d(TAG, "publishResults: data size: ${data.size}")

            if (data.isEmpty()) {
                viewModel.setEventLineListSearchResultNotFoundToTrue()
            } else {
                viewModel.setEventLineListSearchResultNotFoundToFalse()

            }
        }

    }

    class ViewHolder private constructor(val binding: ListItemProblemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Problem) {
            binding.problemTextView.text = item.desc1
            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemProblemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


}

