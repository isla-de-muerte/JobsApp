package com.example.jobsapp.presentation.employer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.ApplicationStatusDto
import com.example.jobsapp.data.dto.CategoryResponse
import com.example.jobsapp.data.dto.EmployerApplicationResponse
import com.example.jobsapp.data.dto.VacancyCreateRequest
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.data.dto.WorkFormatDto
import com.example.jobsapp.data.remote.ApplicationMessageApi
import com.example.jobsapp.data.remote.CategoryApi
import com.example.jobsapp.data.remote.EmployerApi
import kotlinx.coroutines.launch

data class EmployerApplicationUiModel(
    val application: EmployerApplicationResponse,
    val unreadCount: Int
)

sealed class EmployerUiState {
    data object Loading : EmployerUiState()

    data class Success(
        val vacancies: List<VacancyResponse>
    ) : EmployerUiState()

    data class Error(
        val message: String
    ) : EmployerUiState()
}

sealed class EmployerApplicationsUiState {
    data object Loading : EmployerApplicationsUiState()

    data class Success(
        val applications: List<EmployerApplicationUiModel>
    ) : EmployerApplicationsUiState()

    data class Error(
        val message: String
    ) : EmployerApplicationsUiState()
}

sealed class EmployerActionState {
    data object Idle : EmployerActionState()
    data object Loading : EmployerActionState()

    data class Success(
        val message: String
    ) : EmployerActionState()

    data class Error(
        val message: String
    ) : EmployerActionState()
}

sealed class CategoriesUiState {
    data object Loading : CategoriesUiState()

    data class Success(
        val categories: List<CategoryResponse>
    ) : CategoriesUiState()

    data class Error(
        val message: String
    ) : CategoriesUiState()
}

class EmployerViewModel(
    private val employerApi: EmployerApi,
    private val categoryApi: CategoryApi,
    private val messageApi: ApplicationMessageApi
) : ViewModel() {

    private val _state = MutableLiveData<EmployerUiState>()
    val state: LiveData<EmployerUiState> = _state

    private val _applicationsState = MutableLiveData<EmployerApplicationsUiState>()
    val applicationsState: LiveData<EmployerApplicationsUiState> = _applicationsState

    private val _actionState = MutableLiveData<EmployerActionState>(EmployerActionState.Idle)
    val actionState: LiveData<EmployerActionState> = _actionState

    private val _categoriesState = MutableLiveData<CategoriesUiState>()
    val categoriesState: LiveData<CategoriesUiState> = _categoriesState

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoriesUiState.Loading

            try {
                val categories = categoryApi.getCategories()
                _categoriesState.value = CategoriesUiState.Success(categories)
            } catch (e: Exception) {
                _categoriesState.value = CategoriesUiState.Error(
                    e.message ?: "Не удалось загрузить категории"
                )
            }
        }
    }

    fun loadMyVacancies() {
        viewModelScope.launch {
            _state.value = EmployerUiState.Loading

            try {
                val vacancies = employerApi.getMyVacancies()
                _state.value = EmployerUiState.Success(vacancies)
            } catch (e: Exception) {
                _state.value = EmployerUiState.Error(
                    e.message ?: "Не удалось загрузить вакансии"
                )
            }
        }
    }

    fun loadApplicationsByVacancy(vacancyId: String) {
        viewModelScope.launch {
            _applicationsState.value = EmployerApplicationsUiState.Loading

            try {
                val applications = employerApi.getApplicationsByVacancy(vacancyId)
                val unreadCounts = messageApi.getUnreadCounts()
                    .associateBy { it.applicationId }

                val uiItems = applications.map { application ->
                    EmployerApplicationUiModel(
                        application = application,
                        unreadCount = unreadCounts[application.id]?.unreadCount ?: 0
                    )
                }

                _applicationsState.value = EmployerApplicationsUiState.Success(uiItems)
            } catch (e: Exception) {
                _applicationsState.value = EmployerApplicationsUiState.Error(
                    e.message ?: "Не удалось загрузить отклики"
                )
            }
        }
    }

    fun updateApplicationStatus(
        vacancyId: String,
        applicationId: String,
        status: ApplicationStatusDto,
        message: String?
    ) {
        viewModelScope.launch {
            _actionState.value = EmployerActionState.Loading

            try {
                employerApi.updateApplicationStatus(
                    applicationId = applicationId,
                    status = status,
                    message = message
                )

                _actionState.value = EmployerActionState.Success("Статус обновлён")
                loadApplicationsByVacancy(vacancyId)
            } catch (e: Exception) {
                _actionState.value = EmployerActionState.Error(
                    e.message ?: "Не удалось обновить статус"
                )
            }
        }
    }

    fun createVacancy(
        categoryId: String,
        title: String,
        description: String,
        requirements: String,
        salaryFrom: String,
        salaryTo: String,
        workFormat: WorkFormatDto
    ) {
        if (categoryId.isBlank()) {
            _actionState.value = EmployerActionState.Error("Выберите категорию")
            return
        }

        if (title.isBlank()) {
            _actionState.value = EmployerActionState.Error("Введите название вакансии")
            return
        }

        if (description.length < 10) {
            _actionState.value = EmployerActionState.Error("Описание должно быть минимум 10 символов")
            return
        }

        viewModelScope.launch {
            _actionState.value = EmployerActionState.Loading

            try {
                employerApi.createVacancy(
                    VacancyCreateRequest(
                        categoryId = categoryId.trim(),
                        title = title.trim(),
                        description = description.trim(),
                        requirements = requirements.trim().ifBlank { null },
                        salaryFrom = salaryFrom.toIntOrNull(),
                        salaryTo = salaryTo.toIntOrNull(),
                        workFormat = workFormat
                    )
                )

                _actionState.value = EmployerActionState.Success("Вакансия создана")
            } catch (e: Exception) {
                _actionState.value = EmployerActionState.Error(
                    e.message ?: "Не удалось создать вакансию"
                )
            }
        }
    }

    fun publishVacancy(vacancyId: String) {
        viewModelScope.launch {
            try {
                employerApi.publishVacancy(vacancyId)
                _actionState.value = EmployerActionState.Success("Вакансия опубликована")
                loadMyVacancies()
            } catch (e: Exception) {
                _actionState.value = EmployerActionState.Error(
                    e.message ?: "Не удалось опубликовать вакансию"
                )
            }
        }
    }

    fun resumeVacancy(vacancyId: String) {
        viewModelScope.launch {
            try {
                employerApi.resumeVacancy(vacancyId)
                _actionState.value = EmployerActionState.Success("Вакансия снова открыта")
                loadMyVacancies()
            } catch (e: Exception) {
                _actionState.value = EmployerActionState.Error(
                    e.message ?: "Не удалось снова открыть вакансию"
                )
            }
        }
    }

    fun archiveVacancy(vacancyId: String) {
        viewModelScope.launch {
            try {
                employerApi.archiveVacancy(vacancyId)
                _actionState.value = EmployerActionState.Success("Вакансия архивирована")
                loadMyVacancies()
            } catch (e: Exception) {
                _actionState.value = EmployerActionState.Error(
                    e.message ?: "Не удалось архивировать вакансию"
                )
            }
        }
    }
}

class EmployerViewModelFactory(
    private val employerApi: EmployerApi,
    private val categoryApi: CategoryApi,
    private val messageApi: ApplicationMessageApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(EmployerViewModel::class.java)) {
            return EmployerViewModel(
                employerApi = employerApi,
                categoryApi = categoryApi,
                messageApi = messageApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}