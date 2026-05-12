package com.example.jobsapp.data.token

import android.content.Context
import com.example.jobsapp.data.dto.UserRoleDto

class TokenStorage(
    context: Context
) {

    private val prefs = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

    fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun saveRole(role: UserRoleDto) {
        prefs.edit()
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getRole(): UserRoleDto? {
        val value = prefs.getString(KEY_ROLE, null) ?: return null
        return runCatching { UserRoleDto.valueOf(value) }.getOrNull()
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ROLE)
            .apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "role"
    }
}