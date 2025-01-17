package com.sspl.core


sealed class ApiStates<out T> {
    data class Success<out T>(val data: T?) : ApiStates<T>()
    data class Failure(val msg: String?) : ApiStates<Nothing>()
    data object Loading : ApiStates<Nothing>()
    data object Empty : ApiStates<Nothing>()
}