package com.example.roti999.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    sealed class NavDestination {
        object Home : NavDestination()
        object Authentication : NavDestination()
    }

    private val _navDestination = MutableStateFlow<NavDestination?>(null)
    val navDestination: StateFlow<NavDestination?> = _navDestination

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                _navDestination.value = NavDestination.Home
            } else {
                _navDestination.value = NavDestination.Authentication
            }
        }
    }
}