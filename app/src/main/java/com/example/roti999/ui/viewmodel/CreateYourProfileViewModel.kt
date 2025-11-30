package com.example.roti999.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.database.local.LocalDatabase
import com.example.roti999.data.model.User
import com.example.roti999.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateYourProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDatabase: LocalDatabase
) : ViewModel() {

    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        object Success : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val localUser = localDatabase.getUser()
            if (localUser != null) {
                _user.value = localUser
            } else {
                userRepository.getCurrentUser {
                    _user.value = it
                }
            }
        }
    }

    fun editUser(name: String, address: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            if (name.isNotEmpty() && address.isNotEmpty()) {
                val currentUser = user.value
                if (currentUser != null) {
                    if (currentUser.name != name || currentUser.address != address) {
                        val user = currentUser.copy(name = name, address = address)
                        userRepository.createUser(user) { success ->
                            if (success) {
                                _user.value = user
                                localDatabase.setUser(user)
                                _profileState.value = ProfileState.Success
                            } else {
                                _profileState.value = ProfileState.Error("Failed to save profile")
                            }
                        }
                    } else {
                        _profileState.value = ProfileState.Error("No changes to save")
                    }
                } else {
                    _profileState.value = ProfileState.Error("User not logged in")
                }
            } else {
                _profileState.value = ProfileState.Error("Please fill in all fields")
            }
        }
    }
}
