package com.example.localgrub.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.localgrub.data.model.GetUser
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val USER = "user"
    }

    fun setUser(user: GetUser?) {
        sharedPreferences.edit {
            if (user == null) {
                remove(USER)
            } else {
                // Serialize the object to a JSON string
                putString(USER, Json.encodeToString(user))
            }
        }
    }

    fun getUser(): GetUser? {
        val jsonString = sharedPreferences.getString(USER, null)
        if (jsonString.isNullOrEmpty() || jsonString == "null") return null

        return try {
            // Deserialize the JSON string back into the object
            Json.decodeFromString<GetUser>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}