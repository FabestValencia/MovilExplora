package com.example.movilexplora.features.verificationcode

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VerificationCodeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
) : ViewModel() {
    private val _verificationResult = MutableStateFlow<RequestResult?>(null)
    val verificationResult: StateFlow<RequestResult?> = _verificationResult.asStateFlow()

    private var currentUserId: String? = null

    private val _resendResult = MutableStateFlow<RequestResult?>(null)
    val resendResult: StateFlow<RequestResult?> = _resendResult.asStateFlow()

    fun initUserId(userId: String) {
        currentUserId = userId
    }

    fun checkVerificationStatus() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _verificationResult.value = RequestResult.Loading
            // verifyCode ahora intenta recargar al usuario y ver si isEmailVerified es true
            val user = userRepository.verifyCode(userId, "")
            _verificationResult.value = if (user != null) {
                RequestResult.Success(resources.getString(R.string.verification_code_success), user)
            } else {
                RequestResult.Failure(resources.getString(R.string.verification_code_invalid))
            }
        }
    }

    fun resendCode() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _resendResult.value = RequestResult.Loading
            userRepository.resendVerificationCode(userId)
            _resendResult.value = RequestResult.Success(resources.getString(R.string.forgot_password_link_sent))
        }
    }

    fun resetResult() {
        _verificationResult.value = null
        _resendResult.value = null
    }
}
