package co.ltlabs.ltmechanic.repository.co

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.database.CORequestDao
import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.network.ApiCO
import timber.log.Timber

class COPagingDataSource(
    private val service: ApiCO,
    private val status: String,
    private val lineSelected: String,
    private val coRequestDao: CORequestDao
) : PagingSource<Int, COItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, COItem> {
        val page = params.key ?: 1
        Timber.tag("COPagingDataSource").d(page.toString())
        return try {
            val type = when (COStatusType.fromStringToType(status)) {
                COStatusType.Closed -> "0"
                COStatusType.Ready -> "3"
                else -> "1,2"
            }
            val result = service.getCOList(lineSelected, page, 20, type)
            val response = result.body()
            val data = response?.result
            val meta = response?.meta.apply {
                this?.status = status
            }
            meta?.let {
                coRequestDao.insert(it)
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

    override fun getRefreshKey(state: PagingState<Int, COItem>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}