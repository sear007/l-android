package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.util.Line
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.SetupLineViewModel

private const val TAG = "ListAdapter"

class LineListAdapter constructor(val viewModel: SetupLineViewModel)
    : RecyclerView.Adapter<LineListAdapter.ViewHolder>(), Filterable {

    var index = -1
    var data = mutableListOf<Line>()
        set(value) {
            field = value
            dataFull.addAll(value)
            notifyDataSetChanged()
        }

    var dataFull = mutableListOf<Line>()

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val currentLine = viewModel.lineList[position]

        holder.binding.lineContainer.setOnClickListener {
            index = position
            viewModel.selectedLine = item.name
            notifyDataSetChanged()
        }
        
        if (viewModel.popupFirstOpen) {
            if (currentLine.checked) {
                viewModel.popupFirstOpen = false
                Log.d(TAG, "onBindViewHolder: ${currentLine.checked}")
                holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1d547a"))
                holder.binding.lineTextView.setTextColor(Color.WHITE)
            } else {
                if (position % 2 != 0) {
                    holder.binding.lineContainer.setBackgroundColor(Color.WHITE)
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                } else {
                    holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#f2f2f2"))
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                }
            }
        } else {
            if (index == position) {
                currentLine.checked = true
                holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1d547a"))
                holder.binding.lineTextView.setTextColor(Color.WHITE)
            } else {
                currentLine.checked = false
                if (position % 2 != 0) {
                    holder.binding.lineContainer.setBackgroundColor(Color.WHITE)
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                } else {
                    holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#f2f2f2"))
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
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

            val filteredList = mutableListOf<Line>()

            if (constraint == null || constraint.isEmpty() || constraint == "") {
                filteredList.addAll(dataFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()
                for (line in dataFull) {
                    if (line.name.toLowerCase().contains(filterPattern)) {
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
            data.addAll(results?.values as Collection<Line>)

            notifyDataSetChanged()

            if (data.isEmpty()) {
                viewModel.setEventLineListSearchResultNotFoundToTrue()
            } else {
                viewModel.setEventLineListSearchResultNotFoundToFalse()

            }
        }

    }

    class ViewHolder private constructor(val binding: ListItemLineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Line) {
            binding.line = item.name
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


}
