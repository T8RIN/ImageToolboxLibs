package io.rektplorer64.pickerkt.common.data.datasource

import io.rektplorer64.pickerkt.common.data.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

abstract class DataSource<W> {

    private val triggerFlow = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow: Flow<Result<W>> = triggerFlow
        .flatMapLatest {
            flow {
                emit(Result.Loading)
                emit(fetchData())
            }
        }

    abstract suspend fun fetchData(): Result<W>

    fun refresh() {
        triggerFlow.tryEmit(triggerFlow.value + 1)
    }
}