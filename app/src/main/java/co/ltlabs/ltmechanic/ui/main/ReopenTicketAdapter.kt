package co.ltlabs.ltmechanic.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemReopenTicketBinding
import co.ltlabs.ltmechanic.domain.TicketLogs
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReopenTicketAdapter @Inject constructor(
    private val languageJsonObject: JSONObject,
    private val format: SimpleDateFormat
) :
    ListAdapter<TicketLogs, ReopenTicketAdapter.ViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemReopenTicketBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindingItem(position)
    }

    inner class ViewHolder(val binding: ItemReopenTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {


        @SuppressLint("SetTextI18n")
        fun bindingItem(position: Int) {
            val data = getItem(position) ?: return
            binding.apply {
                tvSolutionTag.text =
                    "${languageJsonObject.getTranslation("SOLUTION")} ${position + 1}"
                tvSolution.text = data.solution
                tvRemarkTag.text =
                    "${languageJsonObject.getTranslation("REMARKS")} ${position + 1}"
                tvRemark.text = if (data.remarks.isNullOrEmpty()) "-" else data.remarks
                tvRepairedTag.text = languageJsonObject.getTranslation("REPAIRED BY")
                tvRepaired.text = data.repairedBy
                tvReopenByTag.text = languageJsonObject.getTranslation("RE-OPENED BY")
                tvReopenBy.text = data.reopenedBy ?: "-"
                tvReopenTimeTag.text = languageJsonObject.getTranslation("RE-OPENED TIME")
                tvReopenTime.text = DateUtil.formatToDateAndTime(data.reopenedDt)
            }
        }

    }

    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<TicketLogs>() {
            override fun areItemsTheSame(
                oldItem: TicketLogs,
                newItem: TicketLogs
            ): Boolean {
                return oldItem.reportedDt == newItem.reportedDt
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: TicketLogs,
                newItem: TicketLogs
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}