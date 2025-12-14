package com.example.roti999.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.roti999.domain.model.User
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    private val gson = Gson()
    companion object {
        private const val USER = "user"
    }

    fun setUser(user: User?) {
        sharedPreferences.edit {
            if (user == null) {
                remove(USER)
            } else {
                putString(USER, gson.toJson(user))
            }
        }
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString(USER, null) ?: return null
        if (json == "null") return null
        return gson.fromJson(json, User::class.java)
    }
}