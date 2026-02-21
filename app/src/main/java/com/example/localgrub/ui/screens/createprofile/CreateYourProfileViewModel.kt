package com.example.localgrub.ui.screens.createprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrub.data.model.GetUser
import com.example.localgrub.domain.usecase.UserUseCase
import com.example.localgrub.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateYourProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUIState>(ProfileUIState.Idle)
    val uiState: StateFlow<ProfileUIState> get() = _uiState.asStateFlow()
    private val _user = MutableStateFlow<GetUser?>(null)
    val user: StateFlow<GetUser?> get() = _user.asStateFlow()

    fun onSetUser(user: GetUser) {
        _user.value = user
    }

    fun editUser(name: String, address: String) {
        _uiState.value = ProfileUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = ProfileUIState.NoInternet
            return
        }

        val currentUser = user.value
        if (currentUser != null) {
            val validationMsgForName = validateName(name)
            val validationMsgForAddress = validateAddress(address)

            if (validationMsgForAddress != null || validationMsgForName != null) {
                _uiState.value = ProfileUIState.ValidationErrors(
                    msgForName = validationMsgForName,
                    msgForAddress = validationMsgForAddress,
                    null
                )
                return
            } else if (name == currentUser.name && address == currentUser.address) {
                _uiState.value = ProfileUIState.ValidationErrors(
                    null,
                    null,
                    "No changes to save."
                )
                return
            }

            val user = currentUser.copy(
                name = name,
                address = address,
                profileCompleted = true,
                createAt = System.currentTimeMillis()
            )

            viewModelScope.launch(Dispatchers.IO) {
                _uiState.value = userUseCase.saveUser(user)
            }
        }
    }

    private fun validateAddress(address: String): String? {
        if (address.isBlank()) return "Address must not be null."

        return null
    }

    private fun validateName(name: String): String? {
        if (name.isBlank()) return "Name must not be null."
        if (name.length < 3) return "Name must be at least 3 characters long."

        return null
    }
}