package co.ltlabs.ltmechanic.repository.paging

import androidx.room.Entity
import androidx.room.PrimaryKey

data class BasePaging<T>(
    val result: List<T>? = null,
    val meta: Meta? = null
)

@Entity
data class Meta(
    @PrimaryKey
    var status: String,
    val totalRecord: Int,
    val page: Int,
    val pageSize: Int
)