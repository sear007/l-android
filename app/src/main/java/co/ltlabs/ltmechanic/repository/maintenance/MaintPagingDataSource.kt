package co.ltlabs.ltmechanic.repository.maintenance

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.ltlabs.ltmechanic.database.MaintMetaDao
import co.ltlabs.ltmechanic.domain.maint.MaintItem
import co.ltlabs.ltmechanic.network.ApiMaint

class MaintPagingDataSource(
    private val dao: MaintMetaDao,
    private val service: ApiMaint,
    private val type: String,
    private val machine: String?,
    private val lineSelected: String? = null,
    private val areaSelected: String? = null
) : PagingSource<Int, MaintItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MaintItem> {
        val page = params.key ?: 1
        return try {
            val result = service.getMaints(page, 20, type, machine, lineSelected, areaSelected)
            val response = result.body()
            val data = response?.maints
            val meta = response?.meta.apply {
                this?.type = type
            }
            meta?.let {
                dao.insert(it)
            }

            val nextPageNumber =
                if (data?.isEmpty() == true) {
                    null
                } else {
                    meta?.page?.plus(1)
                }

            LoadResult.Page(
                data = data.orEmpty(),
                prevKey = null,
                nextKey = nextPageNumber
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MaintItem>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}