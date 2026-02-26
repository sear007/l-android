package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.databinding.ListItemTicketChecklistBinding
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.domain.TicketChecklist
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel

private const val TAG = "ReportedListAdapter"

class LineLeaderTicketChecklistListAdapter constructor()
    : RecyclerView.Adapter<LineLeaderTicketChecklistListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<TicketChecklist>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        var checked = item.checked

        if (checked) {
            holder.binding.textViewCheck.setBackgroundColor(Color.parseColor("#0FBC88"))
        } else {
            holder.binding.textViewCheck.setBackgroundColor(Color.parseColor("#1D5A84CC"))
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemTicketChecklistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketChecklist) {

            binding.textViewTask.text = item.task

            if (item.subtask) {
                val params = binding.textViewCheck.layoutParams as ViewGroup.MarginLayoutParams
                params.leftMargin = 35
                binding.textViewCheck.layoutParams = params
            } else {
                val params = binding.textViewCheck.layoutParams as ViewGroup.MarginLayoutParams
                params.leftMargin = 0
                binding.textViewCheck.layoutParams = params
            }

            if (item.checked) {
                binding.textViewCheck.setBackgroundColor(Color.parseColor("#0FBC88"))
            }

            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemTicketChecklistBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


}