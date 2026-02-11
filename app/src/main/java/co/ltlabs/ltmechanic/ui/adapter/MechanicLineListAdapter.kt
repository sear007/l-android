package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.util.Line
import co.ltlabs.ltmechanic.util.LineUtil
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicHomeViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel

private const val TAG = "MechanicLineListAdapter";

class MechanicLineListAdapter constructor(val viewModel: MechanicHomeViewModel, val lineViewModel: LineViewModel)
    : RecyclerView.Adapter<MechanicLineListAdapter.ViewHolder>(), Filterable{

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    private var tracker: SelectionTracker<Long>? = null

    fun setTracker(tracker: SelectionTracker<Long>?) {
        this.tracker = tracker
    }

    var index = -1
    var data = mutableListOf<MfgLine>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dataFull = listOf<MfgLine>()

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        var checked = item.checked ?: false

        val parent = holder.binding.lineContainer

        parent.apply {
            if (checked) {
                setBackgroundColor(Color.parseColor("#1d547a"))
                holder.binding.lineTextView.setTextColor(Color.WHITE)
            } else {
                if (position % 2 != 0) {
                    setBackgroundColor(Color.WHITE)
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                } else {
                    setBackgroundColor(Color.parseColor("#f2f2f2"))
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                }
            }

            setOnClickListener {
                checked = !checked
                data[position].checked = checked

//                if (!checked) {
                    LineUtil.uncheckedLines.add(data[position])
//                }

//                lineViewModel.updateMfgLine(data)
                lineViewModel.updateMfgLineByUserArea(data)
//                lineViewModel.updateMfgLineToChecked(position, data[position])
                notifyItemChanged(holder.adapterPosition)
            }
        }

        holder.bind(item, position)
    }

    override fun getFilter(): Filter = lineFilter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    private val lineFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList = mutableListOf<MfgLine>()

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

            Log.d(TAG, "publishResults: data size: ${data.size}")
            Log.d(TAG, "publishResults: dataFull size: ${dataFull.size}")

            notifyDataSetChanged()

            if (data.isEmpty()) {
                viewModel.setEventLineListSearchResultNotFoundToTrue()
            } else {
                viewModel.setEventLineListSearchResultNotFoundToFalse()

            }

        }

    }


    class ViewHolder private constructor(val binding: ListItemLineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MfgLine, position: Int) {
            binding.line = item.mfgLine
            binding.executePendingBindings()

        }


        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object: ItemDetailsLookup.ItemDetails<Long>() {

                override fun getPosition(): Int = adapterPosition

                override fun getSelectionKey(): Long? = itemId

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

class MechanicLineListener(val clickListener: (line: String) -> Unit) {
    fun onClick(line: String) = clickListener(line)
}