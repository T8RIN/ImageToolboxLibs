package io.rektplorer64.pickerkt.common

import io.rektplorer64.pickerkt.common.data.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

fun <T> loadingResultFlowOf(flow: Flow<Result<T>>): Flow<Result<T>> {
    return flow {
        emit(Result.Loading)
        emitAll(flow)
    }
}