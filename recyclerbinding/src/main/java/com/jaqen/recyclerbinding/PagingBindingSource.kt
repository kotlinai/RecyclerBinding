package com.jaqen.recyclerbinding

import androidx.lifecycle.Lifecycle
import androidx.paging.*
import kotlinx.coroutines.CoroutineScope

typealias SourceLoader<Key, Value> = (PagingBindingSource.LoadParams<Key>) -> PagingBindingSource.LoadResult<Key, Value>

private class BindingPagingSource<Key : Any, Value : Any>(private val loadSource: SourceLoader<Key, Value>): PagingSource<Key, Value>() {
    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        val result = loadSource.invoke(
            PagingBindingSource.LoadParams(
                params.key,
                params.loadSize,
                params.placeholdersEnabled
            )
        )

        return if (result is PagingBindingSource.LoadResult.Error){
            LoadResult.Error(result.throwable)
        }else {
            result as PagingBindingSource.LoadResult.Page
            LoadResult.Page(result.data, result.prevKey, result.nextKey)
        }
    }
}

fun <Key : Any, Value : Any> createSource(creator: PagingBindingSource.Creator<Key, Value>.() -> Unit)
        : PagingBindingSource<Key, Value>
        = PagingBindingSource.Creator<Key, Value>().apply { creator() }.create()

class PagingBindingSource<K : Any, V : Any> private constructor(
    private val pagingSource: PagingSource<K, V>,
    private val config: PagingConfig = PagingConfig(20)){
    val liveData = Pager(config){pagingSource}.liveData

    fun refresh(){
        pagingSource.invalidate()
    }

    fun catchData(lifecycle: Lifecycle) = liveData.cachedIn(lifecycle)
    fun catchData(scope: CoroutineScope) = liveData.cachedIn(scope)

    class Creator<Key: Any, Value: Any>(){
        private var pageSize: Int = 20
        //var sourceLoader: SourceLoader<Key, Value> =
        private var pagingSource: PagingSource<Key, Value> = BindingPagingSource {
            LoadResult.Error(Throwable("No sourceLoader Provided"))
        }

        fun pageSize(size: Int): Creator<Key, Value> {
            this.pageSize = size

            return this
        }

        fun sourceLoader(loader: SourceLoader<Key, Value>): Creator<Key, Value> {
            //this.sourceLoader = loader
            this.pagingSource = BindingPagingSource(loader)

            return this
        }

        fun pagingSource(pagingSource: PagingSource<Key, Value>): Creator<Key, Value> {
            this.pagingSource = pagingSource

            return this
        }

        fun create(): PagingBindingSource<Key, Value>
                = PagingBindingSource(pagingSource, PagingConfig(pageSize))

    }

    data class LoadParams<Key: Any>(
        val key: Key?,
        val loadSize: Int,
        val placeholdersEnabled: Boolean)

    sealed class LoadResult<K : Any, V : Any>{
        data class Page<K : Any , V : Any>(
            val prevKey: K?,
            val nextKey: K?,
            val data: List<V>
        ): LoadResult<K, V>()

        data class Error<Key : Any, Value : Any>(
            val throwable: Throwable
        ) : LoadResult<Key, Value>()
    }
}
