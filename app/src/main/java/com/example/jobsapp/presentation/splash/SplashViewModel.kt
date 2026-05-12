package com.example.jobsapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jobsapp.data.dto.UserRoleDto
import com.example.jobsapp.data.token.TokenStorage

sealed class SplashDestination {
    data object Login : SplashDestination()
    data object ApplicantHome : SplashDestination()
    data object EmployerHome : SplashDestination()
}

class SplashViewModel(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    fun resolveDestination(): SplashDestination {
        val token = tokenStorage.getAccessToken()
        val role = tokenStorage.getRole()

        if (token.isNullOrBlank() || role == null) {
            return SplashDestination.Login
        }

        return when (role) {
            UserRoleDto.APPLICANT -> SplashDestination.ApplicantHome
            UserRoleDto.EMPLOYER -> SplashDestination.EmployerHome
        }
    }
}

class SplashViewModelFactory(
    private val tokenStorage: TokenStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            return SplashViewModel(tokenStorage) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}