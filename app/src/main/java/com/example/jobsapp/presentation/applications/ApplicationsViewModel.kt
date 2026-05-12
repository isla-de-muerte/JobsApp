package com.example.jobsapp.presentation.applications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.ApplicationResponse
import com.example.jobsapp.data.remote.ApplicationApi
import com.example.jobsapp.data.remote.ApplicationMessageApi
import com.example.jobsapp.data.remote.VacancyApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

data class ApplicationUiModel(
    val application: ApplicationResponse,
    val vacancyTitle: String,
    val unreadCount: Int
)

sealed class ApplicationsUiState {
    data object Loading : ApplicationsUiState()

    data class Success(
        val applications: List<ApplicationUiModel>
    ) : ApplicationsUiState()

    data class Error(
        val message: String
    ) : ApplicationsUiState()
}

class ApplicationsViewModel(
    private val applicationApi: ApplicationApi,
    private val vacancyApi: VacancyApi,
    private val messageApi: ApplicationMessageApi
) : ViewModel() {

    private val _state = MutableLiveData<ApplicationsUiState>()
    val state: LiveData<ApplicationsUiState> = _state

    fun loadApplications() {
        viewModelScope.launch {
            _state.value = ApplicationsUiState.Loading

            try {
                val applications = applicationApi.getMyApplications()
                val unreadCounts = messageApi.getUnreadCounts()
                    .associateBy { it.applicationId }

                val uiItems = applications.map { application ->
                    async {
                        val vacancy = vacancyApi.getVacancyById(application.vacancyId)

                        ApplicationUiModel(
                            application = application,
                            vacancyTitle = vacancy.title,
                            unreadCount = unreadCounts[application.id]?.unreadCount ?: 0
                        )
                    }
                }.awaitAll()

                _state.value = ApplicationsUiState.Success(
                    applications = uiItems
                )
            } catch (e: Exception) {
                _state.value = ApplicationsUiState.Error(
                    message = e.message ?: "Не удалось загрузить отклики"
                )
            }
        }
    }
}

class ApplicationsViewModelFactory(
    private val applicationApi: ApplicationApi,
    private val vacancyApi: VacancyApi,
    private val messageApi: ApplicationMessageApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ApplicationsViewModel::class.java)) {
            return ApplicationsViewModel(
                applicationApi = applicationApi,
                vacancyApi = vacancyApi,
                messageApi = messageApi
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}