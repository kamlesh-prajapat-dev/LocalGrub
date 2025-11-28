package com.example.roti999.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roti999.data.model.User
import com.example.roti999.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateYourProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        object Success : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    private val _profileState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val profileState: LiveData<ProfileState> = _profileState

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    init {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            _user.value = User(
                uid = currentUser?.uid ?: "",
                phoneNumber = currentUser?.phoneNumber ?: ""
            )
        }
    }

    fun createUser(name: String, address: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            if (name.isNotEmpty() && address.isNotEmpty()) {
                val currentUser = _user.value
                if (currentUser != null) {
                    val user = currentUser.copy(name = name, address = address)
                    userRepository.createUser(user) { success ->
                        if (success) {
                            _profileState.value = ProfileState.Success
                            _user.postValue(user)
                        } else {
                            _profileState.value = ProfileState.Error("Failed to save profile")
                        }
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
