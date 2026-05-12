package com.example.jobsapp.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.ApplicantProfileRequest
import com.example.jobsapp.data.dto.ApplicantProfileResponse
import com.example.jobsapp.data.remote.ProfileApi
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Idle : ProfileUiState()
    data object Loading : ProfileUiState()

    data class Loaded(
        val profile: ApplicantProfileResponse
    ) : ProfileUiState()

    data class Saved(
        val profile: ApplicantProfileResponse
    ) : ProfileUiState()

    data class Error(
        val message: String
    ) : ProfileUiState()
}

class ProfileViewModel(
    private val profileApi: ProfileApi
) : ViewModel() {

    private val _state = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val state: LiveData<ProfileUiState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading

            try {
                val profile = profileApi.getProfile()
                _state.value = ProfileUiState.Loaded(profile)
            } catch (e: Exception) {
                // Если профиль ещё не создан, это не критично.
                _state.value = ProfileUiState.Idle
            }
        }
    }

    fun saveProfile(
        fullName: String,
        contacts: String,
        skillsRaw: String,
        experience: String,
        education: String,
        portfolioUrl: String
    ) {
        if (fullName.isBlank()) {
            _state.value = ProfileUiState.Error("Введите ФИО")
            return
        }

        if (contacts.isBlank()) {
            _state.value = ProfileUiState.Error("Введите контакты")
            return
        }

        val skills = skillsRaw
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (skills.isEmpty()) {
            _state.value = ProfileUiState.Error("Введите хотя бы один навык")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileUiState.Loading

            try {
                val profile = profileApi.saveProfile(
                    ApplicantProfileRequest(
                        fullName = fullName.trim(),
                        contacts = contacts.trim(),
                        skills = skills,
                        experience = experience.trim().ifBlank { null },
                        education = education.trim().ifBlank { null },
                        portfolioUrl = portfolioUrl.trim().ifBlank { null }
                    )
                )

                _state.value = ProfileUiState.Saved(profile)
            } catch (e: Exception) {
                _state.value = ProfileUiState.Error(
                    e.message ?: "Не удалось сохранить профиль"
                )
            }
        }
    }
}

class ProfileViewModelFactory(
    private val profileApi: ProfileApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                profileApi = profileApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}