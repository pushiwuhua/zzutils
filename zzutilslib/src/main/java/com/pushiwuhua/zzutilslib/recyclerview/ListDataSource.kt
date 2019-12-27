@file:Suppress("unused")

package com.pushiwuhua.zzutilslib.recyclerview

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import kotlin.math.max

/**
 * 对列表封装为PageList的结构
 * wzz created at 2019/10/10 13:44
 */
class ListDataSource<T> internal constructor(private val list: List<T>) : PositionalDataSource<T>() {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        callback.onResult(list, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
    }
}

/**
 * 获取Page对象
 */
@SuppressLint("CheckResult")
fun <T> buildPageDataWithList(list: List<T>): PagedList<T> {
    val myDataSource = ListDataSource(list)
    val pageSize = max(1, list.size)
    val pagedListConfig = PagedList.Config.Builder()
        .setPageSize(pageSize) //分页数据的数量。在后面的DataSource之loadRange中，count即为每次加载的这个设定值。
        .setPrefetchDistance(pageSize)
        .setInitialLoadSizeHint(pageSize)
        .setEnablePlaceholders(false)
        .build()

    return PagedList.Builder(myDataSource, pagedListConfig)
        .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
        .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
        .build()
}
