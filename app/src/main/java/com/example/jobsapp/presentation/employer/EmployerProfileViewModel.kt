package com.example.jobsapp.presentation.employer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.EmployerProfileRequest
import com.example.jobsapp.data.dto.EmployerProfileResponse
import com.example.jobsapp.data.remote.EmployerProfileApi
import kotlinx.coroutines.launch

sealed class EmployerProfileUiState {
    data object Idle : EmployerProfileUiState()
    data object Loading : EmployerProfileUiState()

    data class Loaded(
        val profile: EmployerProfileResponse
    ) : EmployerProfileUiState()

    data class Saved(
        val profile: EmployerProfileResponse
    ) : EmployerProfileUiState()

    data class Error(
        val message: String
    ) : EmployerProfileUiState()
}

class EmployerProfileViewModel(
    private val employerProfileApi: EmployerProfileApi
) : ViewModel() {

    private val _state = MutableLiveData<EmployerProfileUiState>(EmployerProfileUiState.Idle)
    val state: LiveData<EmployerProfileUiState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = EmployerProfileUiState.Loading

            try {
                val profile = employerProfileApi.getProfile()
                _state.value = EmployerProfileUiState.Loaded(profile)
            } catch (e: Exception) {
                // Если профиль ещё не создан, это не критично.
                _state.value = EmployerProfileUiState.Idle
            }
        }
    }

    fun saveProfile(
        companyName: String,
        description: String,
        website: String
    ) {
        if (companyName.isBlank()) {
            _state.value = EmployerProfileUiState.Error("Введите название компании")
            return
        }

        if (companyName.trim().length < 2) {
            _state.value = EmployerProfileUiState.Error("Название компании должно быть минимум 2 символа")
            return
        }

        viewModelScope.launch {
            _state.value = EmployerProfileUiState.Loading

            try {
                val profile = employerProfileApi.saveProfile(
                    EmployerProfileRequest(
                        companyName = companyName.trim(),
                        description = description.trim().ifBlank { null },
                        website = website.trim().ifBlank { null }
                    )
                )

                _state.value = EmployerProfileUiState.Saved(profile)
            } catch (e: Exception) {
                _state.value = EmployerProfileUiState.Error(
                    e.message ?: "Не удалось сохранить профиль компании"
                )
            }
        }
    }
}

class EmployerProfileViewModelFactory(
    private val employerProfileApi: EmployerProfileApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(EmployerProfileViewModel::class.java)) {
            return EmployerProfileViewModel(
                employerProfileApi = employerProfileApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}