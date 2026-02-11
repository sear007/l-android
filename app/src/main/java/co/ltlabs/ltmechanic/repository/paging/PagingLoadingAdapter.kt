package co.ltlabs.ltmechanic.repository.paging

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class PagingLoadingAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<PagingLoadingViewHolder>() {
    override fun onBindViewHolder(holder: PagingLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadingViewHolder {
        return PagingLoadingViewHolder.create(parent, retry)
    }
}