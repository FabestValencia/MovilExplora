package com.example.movilexplora.core.utils

sealed class RequestResult {
    data class Success(val message: String, val data: Any? = null) : RequestResult()
    data class Failure(val errorMessage: String) : RequestResult()
    data object Loading : RequestResult()
}
