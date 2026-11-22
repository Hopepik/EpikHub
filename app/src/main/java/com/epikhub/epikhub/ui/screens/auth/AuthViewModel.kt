package com.epikhub.epikhub.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epikhub.epikhub.data.model.User
import com.epikhub.epikhub.data.repository.AuthRepository
import com.epikhub.epikhub.domain.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isMissingProfile: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUserId: String? get() = authRepository.currentUserId
    val isLoggedIn: Boolean get() = authRepository.isUserLoggedIn()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val uid = authRepository.currentUserId
        if (uid != null) {
            viewModelScope.launch {
                _uiState.value = AuthUiState(isLoading = true)
                when (val result = authRepository.getUserProfile(uid)) {
                    is Resource.Success -> {
                        _uiState.value = AuthUiState(user = result.data)
                    }
                    is Resource.Error -> {
                        if (result.message == "PROFILE_MISSING") {
                            _uiState.value = AuthUiState(
                                isMissingProfile = true,
                                errorMessage = "Your account has no profile document. Please enter your name and phone number to complete setup."
                            )
                        } else {
                            _uiState.value = AuthUiState(errorMessage = result.message)
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(email, pass)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(user = result.data, isSuccess = true)
                }
                is Resource.Error -> {
                    if (result.message == "PROFILE_MISSING") {
                        _uiState.value = AuthUiState(
                            isMissingProfile = true,
                            errorMessage = "Account authenticated! Please enter your details below to create your customer profile."
                        )
                    } else {
                        _uiState.value = AuthUiState(errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun registerCustomer(fullName: String, phone: String, email: String, pass: String) {
        if (fullName.isBlank() || phone.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please fill in all required fields")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.registerCustomer(fullName, phone, email, pass)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(user = result.data, isSuccess = true)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun completeMissingProfile(fullName: String, phone: String) {
        if (fullName.isBlank() || phone.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both your full name and phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.createMissingCustomerProfile(fullName, phone)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(user = result.data, isSuccess = true, isMissingProfile = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateProfile(fullName: String, phone: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.updateUserProfile(uid, fullName, phone)) {
                is Resource.Success -> {
                    checkCurrentUser()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
