package com.example.jobsapp.di

import android.content.Context
import com.example.jobsapp.data.remote.ApiClient
import com.example.jobsapp.data.remote.ApplicationApi
import com.example.jobsapp.data.remote.ApplicationMessageApi
import com.example.jobsapp.data.remote.AuthApi
import com.example.jobsapp.data.remote.CategoryApi
import com.example.jobsapp.data.remote.EmployerApi
import com.example.jobsapp.data.remote.EmployerProfileApi
import com.example.jobsapp.data.remote.ProfileApi
import com.example.jobsapp.data.remote.VacancyApi
import com.example.jobsapp.data.token.TokenStorage
import com.example.jobsapp.presentation.applications.ApplicationsViewModelFactory
import com.example.jobsapp.presentation.auth.AuthViewModelFactory
import com.example.jobsapp.presentation.chat.ApplicationChatViewModelFactory
import com.example.jobsapp.presentation.employer.EmployerProfileViewModelFactory
import com.example.jobsapp.presentation.employer.EmployerViewModelFactory
import com.example.jobsapp.presentation.favorites.FavoritesViewModelFactory
import com.example.jobsapp.presentation.profile.ProfileViewModelFactory
import com.example.jobsapp.presentation.splash.SplashViewModelFactory
import com.example.jobsapp.presentation.vacancies.VacancyViewModelFactory
import io.ktor.client.HttpClient

object AppModule {

    private var tokenStorage: TokenStorage? = null
    private var httpClient: HttpClient? = null

    private var authApi: AuthApi? = null
    private var vacancyApi: VacancyApi? = null
    private var profileApi: ProfileApi? = null
    private var applicationApi: ApplicationApi? = null
    private var applicationMessageApi: ApplicationMessageApi? = null
    private var employerApi: EmployerApi? = null
    private var employerProfileApi: EmployerProfileApi? = null
    private var categoryApi: CategoryApi? = null

    fun provideTokenStorage(
        context: Context
    ): TokenStorage {
        val existing = tokenStorage

        if (existing != null) {
            return existing
        }

        val created = TokenStorage(
            context.applicationContext
        )

        tokenStorage = created

        return created
    }

    private fun provideHttpClient(
        context: Context
    ): HttpClient {
        val existing = httpClient

        if (existing != null) {
            return existing
        }

        val created = ApiClient.create(
            tokenStorage = provideTokenStorage(context)
        )

        httpClient = created

        return created
    }

    fun provideAuthApi(
        context: Context
    ): AuthApi {
        val existing = authApi

        if (existing != null) {
            return existing
        }

        val created = AuthApi(
            client = provideHttpClient(context)
        )

        authApi = created

        return created
    }

    fun provideVacancyApi(
        context: Context
    ): VacancyApi {
        val existing = vacancyApi

        if (existing != null) {
            return existing
        }

        val created = VacancyApi(
            client = provideHttpClient(context)
        )

        vacancyApi = created

        return created
    }

    fun provideProfileApi(
        context: Context
    ): ProfileApi {
        val existing = profileApi

        if (existing != null) {
            return existing
        }

        val created = ProfileApi(
            client = provideHttpClient(context)
        )

        profileApi = created

        return created
    }

    fun provideApplicationApi(
        context: Context
    ): ApplicationApi {
        val existing = applicationApi

        if (existing != null) {
            return existing
        }

        val created = ApplicationApi(
            client = provideHttpClient(context)
        )

        applicationApi = created

        return created
    }

    fun provideApplicationMessageApi(
        context: Context
    ): ApplicationMessageApi {
        val existing = applicationMessageApi

        if (existing != null) {
            return existing
        }

        val created = ApplicationMessageApi(
            client = provideHttpClient(context)
        )

        applicationMessageApi = created

        return created
    }

    fun provideEmployerApi(
        context: Context
    ): EmployerApi {
        val existing = employerApi

        if (existing != null) {
            return existing
        }

        val created = EmployerApi(
            client = provideHttpClient(context)
        )

        employerApi = created

        return created
    }

    fun provideEmployerProfileApi(
        context: Context
    ): EmployerProfileApi {
        val existing = employerProfileApi

        if (existing != null) {
            return existing
        }

        val created = EmployerProfileApi(
            client = provideHttpClient(context)
        )

        employerProfileApi = created

        return created
    }

    fun provideCategoryApi(
        context: Context
    ): CategoryApi {
        val existing = categoryApi

        if (existing != null) {
            return existing
        }

        val created = CategoryApi(
            client = provideHttpClient(context)
        )

        categoryApi = created

        return created
    }

    fun provideAuthViewModelFactory(
        context: Context
    ): AuthViewModelFactory {
        return AuthViewModelFactory(
            authApi = provideAuthApi(context),
            tokenStorage = provideTokenStorage(context)
        )
    }

    fun provideVacancyViewModelFactory(
        context: Context
    ): VacancyViewModelFactory {
        return VacancyViewModelFactory(
            vacancyApi = provideVacancyApi(context)
        )
    }

    fun provideProfileViewModelFactory(
        context: Context
    ): ProfileViewModelFactory {
        return ProfileViewModelFactory(
            profileApi = provideProfileApi(context)
        )
    }

    fun provideApplicationsViewModelFactory(
        context: Context
    ): ApplicationsViewModelFactory {
        return ApplicationsViewModelFactory(
            applicationApi = provideApplicationApi(context),
            vacancyApi = provideVacancyApi(context),
            messageApi = provideApplicationMessageApi(context)
        )
    }

    fun provideFavoritesViewModelFactory(
        context: Context
    ): FavoritesViewModelFactory {
        return FavoritesViewModelFactory(
            vacancyApi = provideVacancyApi(context)
        )
    }

    fun provideEmployerViewModelFactory(
        context: Context
    ): EmployerViewModelFactory {
        return EmployerViewModelFactory(
            employerApi = provideEmployerApi(context),
            categoryApi = provideCategoryApi(context),
            messageApi = provideApplicationMessageApi(context)
        )
    }

    fun provideEmployerProfileViewModelFactory(
        context: Context
    ): EmployerProfileViewModelFactory {
        return EmployerProfileViewModelFactory(
            employerProfileApi = provideEmployerProfileApi(context)
        )
    }

    fun provideSplashViewModelFactory(
        context: Context
    ): SplashViewModelFactory {
        return SplashViewModelFactory(
            tokenStorage = provideTokenStorage(context)
        )
    }

    fun provideApplicationChatViewModelFactory(
        context: Context
    ): ApplicationChatViewModelFactory {
        return ApplicationChatViewModelFactory(
            api = provideApplicationMessageApi(context)
        )
    }

    fun clearSession(
        context: Context
    ) {
        provideTokenStorage(context).clearTokens()
    }
}