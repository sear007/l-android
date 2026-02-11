package co.ltlabs.ltmechanic.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemMaintenanceChecklistBinding
import co.ltlabs.ltmechanic.databinding.ListItemReportedTicketsBinding
import co.ltlabs.ltmechanic.databinding.ListItemTicketChecklistBinding
import co.ltlabs.ltmechanic.domain.MaintenanceChecklist
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.domain.TicketChecklist
import co.ltlabs.ltmechanic.util.CheckListUtil
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceChecklistViewmodel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel

private const val TAG = "ReportedListAdapter"

class MechanicMaintenanceChecklistListAdapter constructor(
    val viewModel: MaintenanceChecklistViewmodel,
    val checklistViewModel: ChecklistViewModel
)
    : RecyclerView.Adapter<MechanicMaintenanceChecklistListAdapter.ViewHolder>() {

    var index = -1
    var data = listOf<MaintenanceChecklist>()
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

        Log.d(TAG, "onBindViewHolder: tag: ${item.tag}")

        if (CheckListUtil.checkListEditable) {
            holder.binding.textViewCheck.setOnClickListener {
                checked = !checked

                item.checked = checked
                data[position].checked = checked
//            ticketViewModel.updateChecklists(item, position)

                if (!item.subtask) {
                    val parents = data.filter { !it.subtask }
                    parents.forEachIndexed { index, parent ->
                        val tagPartial = index + 1
                        Log.d(TAG, "onBindViewHolder: parent.checked: ${parent.checked}")
                        if (parent.checked) {
                            Log.d(TAG, "onBindViewHolder: $tagPartial children size: ${data.filter { it.tag.contains("$tagPartial.") }.size}")
                            data.filter { it.tag.contains("$tagPartial.") }.forEach {

                                it.checked = true
                            }
                        } else {
                            Log.d(TAG, "onBindViewHolder: $tagPartial. children size: ${data.filter { it.tag.contains("$tagPartial.") }.size}")
                            data.filter { it.tag.contains("$tagPartial.") }.forEach {

                                it.checked = false
                            }
                        }
                    }
                } else {
                    val children = data.filter { it.subtask }
                    val checkedChildrenSize = children.filter { it.tag.contains(item.tag.split(".")[0]) }.filter { it.checked }.size
                    val childrenSize = children.filter { it.tag.contains(item.tag.split(".")[0]) }.size
                    Log.d(TAG, "onBindViewHolder: checkedChildrenSize: $checkedChildrenSize")
                    Log.d(TAG, "onBindViewHolder: childrenSize: $childrenSize")
                    if ( checkedChildrenSize == childrenSize ) {
                        data.filter { it.tag == item.tag.split(".")[0] }.forEach {

                            it.checked = true
                        }
                    } else {
                        data.filter { it.tag == item.tag.split(".")[0] }.forEach {

                            it.checked = false
                        }
                    }
                }

                checklistViewModel.updateMaintenanceChecklists(data)

                val selectedCount = data.filter { it.checked }.size
            checklistViewModel.updateSelectedTaskCount(selectedCount)

                notifyItemChanged(holder.adapterPosition)
                notifyDataSetChanged()
            }
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)


    class ViewHolder private constructor(val binding: ListItemMaintenanceChecklistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MaintenanceChecklist) {

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
                val binding = ListItemMaintenanceChecklistBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }


}