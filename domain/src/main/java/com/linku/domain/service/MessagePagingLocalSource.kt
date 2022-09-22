package com.linku.domain.service

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.linku.domain.entity.Message
import com.linku.domain.room.dao.MessageDao

class MessagePagingLocalSource(
    private val messageDao: MessageDao,
    private val cid: Int
) : PagingSource<Int, Message>() {
    override fun getRefreshKey(state: PagingState<Int, Message>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Message> = try {
        val pageNumber = params.key ?: 0
        val loadSize = params.loadSize
        val fromIndex = loadSize * pageNumber
        val endIndex = fromIndex + loadSize - 1
        val data = messageDao.getAllByCid(cid).subList(fromIndex, endIndex)
        LoadResult.Page(
            data = data,
            prevKey = (pageNumber - 1).let { if (it < 0) null else it },
            nextKey = pageNumber + 1
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}