package com.educonsult.crm.util

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String?, val data: T? = null) : Resource<T>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> data
        is Loading -> null
    }

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, data?.let(transform))
    }

    companion object {
        fun <T> loading(): Resource<T> = Loading
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(message: String?, data: T? = null): Resource<T> = Error(message, data)
    }
}
