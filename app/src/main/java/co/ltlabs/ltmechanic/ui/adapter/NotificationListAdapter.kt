package co.ltlabs.ltmechanic.ui.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineStatusAreasBinding
import co.ltlabs.ltmechanic.databinding.ListItemNotificationsBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.domain.Notification
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "NotificationListAdapter";

class NotificationListAdapter constructor(
    val languageJsonObject: JSONObject
)
    : ListAdapter<Notification, NotificationListAdapter.ViewHolder>(NotificationDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemNotificationsBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notification) {

            var dateStr = ""

            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            dateStr = sdf.format(item.dateTime)

            binding.date.text = item.generatedDate
            binding.message.text = languageJsonObject.getTranslation(
                item.message
            )

            if (item.type == "send_request") {
                binding.apply {

                    machineNo.text = item.machineNo
                    location.text = languageJsonObject.getTranslation(
                        item.location
                    )
                    rfid.text = item.rfid
                    subType.text = languageJsonObject.getTranslation(
                        item.subType
                    )

                    if (item.rfid.isNotBlank()) {
                        rfid.visibility = View.VISIBLE
                    }

                    machineNo.visibility = View.VISIBLE
                    location.visibility = View.VISIBLE
                    subType.visibility = View.VISIBLE
                }
            }

            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemNotificationsBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }

}

class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}