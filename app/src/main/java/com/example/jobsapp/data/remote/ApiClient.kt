package com.example.jobsapp.data.remote

import com.example.jobsapp.data.token.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {

    const val BASE_URL = "http://10.0.2.2:8080"

    fun create(
        tokenStorage: TokenStorage
    ): HttpClient {
        val authHeaderPlugin = createClientPlugin("AuthHeaderPlugin") {
            onRequest { request, _ ->
                val token = tokenStorage.getAccessToken()

                if (!token.isNullOrBlank()) {
                    request.header(
                        HttpHeaders.Authorization,
                        "Bearer $token"
                    )
                }
            }
        }

        return HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }

            install(DefaultRequest) {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }

            install(authHeaderPlugin)

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}