package com.example.jobsapp.presentation.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.data.remote.VacancyApi
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()

    data class Success(
        val vacancies: List<VacancyResponse>
    ) : FavoritesUiState()

    data class Error(
        val message: String
    ) : FavoritesUiState()
}

class FavoritesViewModel(
    private val vacancyApi: VacancyApi
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesUiState>()
    val state: LiveData<FavoritesUiState> = _state

    fun loadFavorites() {
        viewModelScope.launch {
            _state.value = FavoritesUiState.Loading

            try {
                val vacancies = vacancyApi.getFavorites()

                _state.value = FavoritesUiState.Success(
                    vacancies = vacancies
                )
            } catch (e: Exception) {
                _state.value = FavoritesUiState.Error(
                    message = e.message ?: "Не удалось загрузить избранное"
                )
            }
        }
    }
}

class FavoritesViewModelFactory(
    private val vacancyApi: VacancyApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(
                vacancyApi = vacancyApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}