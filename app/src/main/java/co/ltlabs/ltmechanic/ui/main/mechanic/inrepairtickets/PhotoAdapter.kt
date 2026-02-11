package co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import co.ltlabs.ltmechanic.databinding.ItemPhotoBinding
import com.bumptech.glide.RequestManager

class PhotoAdapter(
    private val requestManager: RequestManager,
    private val attachments: ArrayList<Uri>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private var itemClick: ((uri: Uri) -> Unit)? = null

    fun setItemClick(item: (uri: Uri) -> Unit) = apply {
        this.itemClick = item
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                itemClick?.invoke(attachments[absoluteAdapterPosition])
            }
        }

        fun bindItem(data: Uri) {
            with(binding) {
                requestManager.load(data)
                    .into(ivAttachment)
            }
        }

    }

    fun updateList(list: List<Uri>) {
        attachments.clear()
        attachments.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemPhotoBinding.inflate(layoutInflater, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindItem(attachments[position])
    }

    override fun getItemCount() = attachments.size

}