package com.wlaz.brainfood.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.wlaz.brainfood.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoggedIn(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(
        if (authRepository.isLoggedIn) AuthState.LoggedIn(authRepository.user!!)
        else AuthState.Idle
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.user
        )

    fun signInWithGoogle(activityContext: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            _authState.value = result.fold(
                onSuccess = { AuthState.LoggedIn(it) },
                onFailure = { AuthState.Error(it.localizedMessage ?: "Error desconocido") }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
