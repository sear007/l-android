package co.ltlabs.ltmechanic.util.popup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineBinding
import co.ltlabs.ltmechanic.viewmodels.shared.SpinnerViewModel
import java.util.*

private const val TAG = "SpinnerListAdapter"

class SpinnerListAdapter constructor(val viewModel: SpinnerViewModel)
    : RecyclerView.Adapter<SpinnerListAdapter.ViewHolder>(), Filterable {

    private var mEvent: ItemListener? = null
    private var mFilterEvent: ItemFilteringListener? = null

    var index = -1
    private val dataFull: MutableList<SpinnerItem>
        get() = viewModel.items.toMutableList()

    var data: MutableList<SpinnerItem> = dataFull
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val currentItem = viewModel.items[position]

        holder.binding.lineContainer.setOnClickListener {
            index = position
            viewModel.selectedItem = item
            mEvent?.onItemClick(item)
            notifyDataSetChanged()
        }
        
        if (viewModel.popupFirstOpen) {
            if (currentItem.checked) {
                viewModel.popupFirstOpen = false
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
                currentItem.checked = true
                holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#1d547a"))
                holder.binding.lineTextView.setTextColor(Color.WHITE)
            } else {
                currentItem.checked = false
                if (position % 2 != 0) {
                    holder.binding.lineContainer.setBackgroundColor(Color.WHITE)
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                } else {
                    holder.binding.lineContainer.setBackgroundColor(Color.parseColor("#f2f2f2"))
                    holder.binding.lineTextView.setTextColor(Color.BLACK)
                }
            }
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun getFilter(): Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList = mutableListOf<SpinnerItem>()

            if (constraint == null || constraint.isEmpty() || constraint == "") {
                filteredList.addAll(dataFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()
                for (line in dataFull) {
                    if (line.name.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(line)
                    }
                }
            }

            val results = FilterResults()
            results.count = filteredList.size
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            data.clear()
            data.addAll(results?.values as MutableList<SpinnerItem>)
            mFilterEvent?.publishResults(data)
            notifyDataSetChanged()

            if (constraint == null || constraint.isEmpty() || constraint == "") {
                viewModel.setEventSearchResultNotFound(false)
                mFilterEvent?.onEventSearchResultNotFound(false)
            } else {
                viewModel.setEventSearchResultNotFound(results.count == 0)
                mFilterEvent?.onEventSearchResultNotFound(results.count == 0)
            }
        }
    }

    fun setOnItemClickListener(event: ItemListener) {
        mEvent = event
    }

    fun setOnFilterListener(filterEvent: ItemFilteringListener) {
        mFilterEvent = filterEvent
    }

    class ViewHolder private constructor(val binding: ListItemLineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SpinnerItem) {
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

    interface ItemListener {
        fun onItemClick(item: SpinnerItem)
    }

    interface ItemFilteringListener {
        fun publishResults(result: MutableList<SpinnerItem>) {}
        fun onEventSearchResultNotFound(isNotFound: Boolean = true)
    }
}
