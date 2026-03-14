package com.example.localgrub.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.localgrub.data.model.firebase.GetUser
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val USER = "user"
        private const val JWT_TOKEN = "jwt_token"
    }

    fun setToken(token: String?) {
        sharedPreferences.edit {
            if (token == null) {
                remove(JWT_TOKEN)
            } else {
                putString(JWT_TOKEN, token)
            }
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(JWT_TOKEN, null)
    }

    fun setUser(user: GetUser?) {
        sharedPreferences.edit {
            if (user == null) {
                remove(USER)
            } else {
                putString(USER, Json.encodeToString(user))
            }
        }
    }

    fun getUser(): GetUser? {
        val jsonString = sharedPreferences.getString(USER, null)
        if (jsonString.isNullOrEmpty() || jsonString == "null") return null

        return try {
            Json.decodeFromString<GetUser>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}