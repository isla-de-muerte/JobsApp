package com.example.jobsapp.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.LoginRequest
import com.example.jobsapp.data.dto.RegisterRequest
import com.example.jobsapp.data.dto.UserRoleDto
import com.example.jobsapp.data.remote.AuthApi
import com.example.jobsapp.data.token.TokenStorage
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()

    data class Success(
        val role: UserRoleDto
    ) : AuthUiState()

    data class Error(
        val message: String
    ) : AuthUiState()
}

class AuthViewModel(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    fun login(
        email: String,
        password: String
    ) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthUiState.Error("Email и пароль обязательны")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState.Loading

            try {
                val response = authApi.login(
                    LoginRequest(
                        email = email.trim(),
                        password = password
                    )
                )

                tokenStorage.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )

                tokenStorage.saveRole(response.user.role)
                _state.value = AuthUiState.Success(response.user.role)

            } catch (e: Exception) {
                _state.value = AuthUiState.Error(
                    e.message ?: "Ошибка авторизации"
                )
            }
        }
    }

    fun register(
        email: String,
        password: String,
        role: UserRoleDto
    ) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthUiState.Error("Email и пароль обязательны")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState.Loading

            try {
                val response = authApi.register(
                    RegisterRequest(
                        email = email.trim(),
                        password = password,
                        role = role
                    )
                )

                tokenStorage.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )

                tokenStorage.saveRole(response.user.role)
                _state.value = AuthUiState.Success(response.user.role)

            } catch (e: Exception) {
                _state.value = AuthUiState.Error(
                    e.message ?: "Ошибка регистрации"
                )
            }
        }
    }
}

class AuthViewModelFactory(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                authApi = authApi,
                tokenStorage = tokenStorage
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}