package com.example.roti999.ui.screens.createprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.dto.User
import com.example.roti999.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateYourProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {


    private val _profileState = MutableStateFlow<ProfileUIState>(ProfileUIState.Idle)
    val profileState: StateFlow<ProfileUIState> get() = _profileState
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    fun onSetUser(user: User) {
        _user.update { user }
    }

    fun loadUser() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user != null) {
                _user.value = user
            } else {
                _profileState.value = ProfileUIState.NavigateToLogin
            }
        }
    }

    fun editUser(name: String, address: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUIState.Loading

            val currentUser = user.value
            if (currentUser != null) {
                val validationResult = validate(name, address, currentUser)
                if (validationResult != null) {
                    _profileState.value = ProfileUIState.ValidationErrors(validationResult)
                    return@launch
                }

                val user = currentUser.copy(name = name, address = address)
                userRepository.createUser(user) { profileState ->
                    _profileState.update { profileState }
                }
            } else {
                _profileState.value = ProfileUIState.ValidationErrors("User not logged in")
            }
        }
    }

    private fun validate(name: String, address: String, currentUser: User?): String? {
        if (name.isBlank() && address.isBlank()) return "Please fill all fields."
        if (name.isBlank()) return "Name must not be null."
        if (address.isBlank()) return "Address must not be null."
        if (name.length < 3) return "Name must be at least 3 characters long."
        if (name == currentUser?.name && address == currentUser.address) return "No changes to save."

        return null
    }
}