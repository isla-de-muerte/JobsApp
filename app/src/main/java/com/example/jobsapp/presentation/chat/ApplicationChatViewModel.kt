package com.example.jobsapp.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jobsapp.data.dto.ApplicationMessageResponse
import com.example.jobsapp.data.remote.ApplicationMessageApi
import kotlinx.coroutines.launch

sealed class ApplicationChatUiState {
    data object Loading : ApplicationChatUiState()

    data class Success(
        val messages: List<ApplicationMessageResponse>
    ) : ApplicationChatUiState()

    data class Error(
        val message: String
    ) : ApplicationChatUiState()
}

class ApplicationChatViewModel(
    private val api: ApplicationMessageApi
) : ViewModel() {

    private val _state = MutableLiveData<ApplicationChatUiState>()
    val state: LiveData<ApplicationChatUiState> = _state

    fun loadMessages(applicationId: String) {
        viewModelScope.launch {
            _state.value = ApplicationChatUiState.Loading

            try {
                val messages = api.getMessages(applicationId)

                api.markAsRead(applicationId)

                _state.value = ApplicationChatUiState.Success(messages)
            } catch (e: Exception) {
                _state.value = ApplicationChatUiState.Error(
                    e.message ?: "Не удалось загрузить сообщения"
                )
            }
        }
    }

    fun sendMessage(applicationId: String, message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            try {
                api.sendMessage(applicationId, message)
                api.markAsRead(applicationId)
                loadMessages(applicationId)
            } catch (e: Exception) {
                _state.value = ApplicationChatUiState.Error(
                    e.message ?: "Не удалось отправить сообщение"
                )
            }
        }
    }
}

class ApplicationChatViewModelFactory(
    private val api: ApplicationMessageApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ApplicationChatViewModel::class.java)) {
            return ApplicationChatViewModel(api) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}