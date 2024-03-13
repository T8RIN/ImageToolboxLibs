package io.rektplorer64.pickerkt.common.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow

sealed class Result<out T> private constructor() {
    data class Success<T>(val data: T) : Result<T>() {
        override fun toString(): String {
            return "Result.Success(data=$data)"
        }
    }

    data class Error<T>(val data: T?, val throwable: Throwable) : Result<T>() {
        override fun toString(): String {
            return "Result.Error(data=$data, throwable=$throwable)"
        }
    }

    object Loading : Result<Nothing>() {
        override fun toString(): String {
            return "Result.Loading()"
        }
    }

    override fun toString(): String {
        return when(this) {
            is Success -> (this as Success).toString()
            is Error -> (this as Error).toString()
            is Loading -> (this as Loading).toString()
        }
    }
}

fun <T, R> Result<T>.transform(transformation: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(data = transformation(data))
        is Result.Error -> Result.Error(data = data?.let { transformation(it) }, throwable = throwable)
        Result.Loading -> Result.Loading
    }
}

fun <T> Result<T?>.defaultIfNull(default: () -> T): Result<T> {
    val data = data ?: default()
    return when (this) {
        is Result.Success -> Result.Success(data = data)
        is Result.Error -> Result.Error(data = data, throwable = throwable)
        Result.Loading -> Result.Loading
    }
}

val <T> Result<T>.data: T?
    get() = when(this) {
        is Result.Success -> data
        is Result.Error -> data
        is Result.Loading -> null
    }

val <T> Result<T>.throwable: Throwable?
    get() = when(this) {
        is Result.Error -> throwable
        else -> null
    }


@Composable
fun <T> Flow<Result<T>>.collectAsResultState(): State<Result<T>> {
    return collectAsState(initial = Result.Loading)
}


