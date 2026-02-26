package co.ltlabs.ltmechanic.ui.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ListItemLineStatusAreasBinding
import co.ltlabs.ltmechanic.databinding.ListItemNotificationsBinding
import co.ltlabs.ltmechanic.databinding.ListItemRequestTypesBinding
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.domain.Notification
import co.ltlabs.ltmechanic.domain.RequestType
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.SendRequestViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.LineStatusViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "RequestTypeListAdapter";

class RequestTypeListAdapter constructor(
    val viewModel: SendRequestViewModel,
    val languageJsonObject: JSONObject
)
    : ListAdapter<RequestType, RequestTypeListAdapter.ViewHolder>(RequestTypeDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.requestType.setOnClickListener {
            viewModel.setSelectedRequestType(item)
        }

        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent, languageJsonObject)


    class ViewHolder private constructor(val binding: ListItemRequestTypesBinding, val languageJsonObject: JSONObject) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RequestType) {

            binding.requestType.text = languageJsonObject.getTranslation(item.name)

            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup, languageJsonObject: JSONObject): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemRequestTypesBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding, languageJsonObject)
            }
        }
    }

}

class RequestTypeDiffCallback : DiffUtil.ItemCallback<RequestType>() {
    override fun areItemsTheSame(oldItem: RequestType, newItem: RequestType): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RequestType, newItem: RequestType): Boolean {
        return oldItem == newItem
    }
}