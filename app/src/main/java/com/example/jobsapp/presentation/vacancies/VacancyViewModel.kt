package com.example.jobsapp.presentation.vacancies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.data.remote.VacancyApi
import kotlinx.coroutines.launch

sealed class VacancyListUiState {
    data object Loading : VacancyListUiState()

    data class Success(
        val vacancies: List<VacancyResponse>
    ) : VacancyListUiState()

    data class Error(
        val message: String
    ) : VacancyListUiState()
}

sealed class VacancyDetailsUiState {
    data object Loading : VacancyDetailsUiState()

    data class Success(
        val vacancy: VacancyResponse
    ) : VacancyDetailsUiState()

    data class Error(
        val message: String
    ) : VacancyDetailsUiState()
}

sealed class VacancyActionUiState {
    data object Idle : VacancyActionUiState()
    data object Loading : VacancyActionUiState()

    data class Success(
        val message: String
    ) : VacancyActionUiState()

    data class Error(
        val message: String
    ) : VacancyActionUiState()
}

class VacancyViewModel(
    private val vacancyApi: VacancyApi
) : ViewModel() {

    private val _state = MutableLiveData<VacancyListUiState>()
    val state: LiveData<VacancyListUiState> = _state

    private val _detailsState = MutableLiveData<VacancyDetailsUiState>()
    val detailsState: LiveData<VacancyDetailsUiState> = _detailsState

    private val _actionState = MutableLiveData<VacancyActionUiState>(VacancyActionUiState.Idle)
    val actionState: LiveData<VacancyActionUiState> = _actionState

    private var currentQuery: String? = null
    private var currentWorkFormat: String? = null

    fun loadVacancies() {
        viewModelScope.launch {
            _state.value = VacancyListUiState.Loading

            try {
                val response = vacancyApi.getVacancies(
                    page = 1,
                    size = 50,
                    query = currentQuery,
                    workFormat = currentWorkFormat
                )

                _state.value = VacancyListUiState.Success(
                    vacancies = response.items
                )
            } catch (e: Exception) {
                _state.value = VacancyListUiState.Error(
                    message = e.message ?: "Не удалось загрузить вакансии"
                )
            }
        }
    }

    fun search(query: String) {
        currentQuery = query.takeIf { it.isNotBlank() }
        loadVacancies()
    }

    fun filterByWorkFormat(workFormat: String?) {
        currentWorkFormat = workFormat
        loadVacancies()
    }

    fun loadVacancyDetails(vacancyId: String) {
        viewModelScope.launch {
            _detailsState.value = VacancyDetailsUiState.Loading

            try {
                val vacancy = vacancyApi.getVacancyById(vacancyId)

                _detailsState.value = VacancyDetailsUiState.Success(
                    vacancy = vacancy
                )
            } catch (e: Exception) {
                _detailsState.value = VacancyDetailsUiState.Error(
                    message = e.message ?: "Не удалось загрузить вакансию"
                )
            }
        }
    }

    fun applyToVacancy(
        vacancyId: String,
        coverLetter: String
    ) {
        viewModelScope.launch {
            _actionState.value = VacancyActionUiState.Loading

            try {
                vacancyApi.applyToVacancy(
                    vacancyId = vacancyId,
                    coverLetter = coverLetter
                )

                _actionState.value = VacancyActionUiState.Success(
                    message = "Отклик успешно отправлен"
                )
            } catch (e: Exception) {
                _actionState.value = VacancyActionUiState.Error(
                    message = e.message ?: "Не удалось откликнуться"
                )
            }
        }
    }

    fun addFavorite(vacancyId: String) {
        viewModelScope.launch {
            _actionState.value = VacancyActionUiState.Loading

            try {
                vacancyApi.addFavorite(vacancyId)

                _actionState.value = VacancyActionUiState.Success(
                    message = "Вакансия добавлена в избранное"
                )
            } catch (e: Exception) {
                _actionState.value = VacancyActionUiState.Error(
                    message = e.message ?: "Не удалось добавить в избранное"
                )
            }
        }
    }
}

class VacancyViewModelFactory(
    private val vacancyApi: VacancyApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(VacancyViewModel::class.java)) {
            return VacancyViewModel(
                vacancyApi = vacancyApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}