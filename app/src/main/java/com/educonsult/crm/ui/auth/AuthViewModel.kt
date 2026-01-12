package com.educonsult.crm.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educonsult.crm.domain.repository.AuthRepository
import com.educonsult.crm.domain.usecase.auth.LoginUseCase
import com.educonsult.crm.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
                if (isLoggedIn) {
                    loadCurrentUser()
                }
            }
        }
    }

    private suspend fun loadCurrentUser() {
        authRepository.getCurrentUser()
            .onSuccess { user ->
                _uiState.update { it.copy(user = user) }
            }
            .onFailure { /* User will remain null */ }
    }

    fun updateEmail(email: String) {
        _uiState.update { state ->
            state.copy(
                loginForm = state.loginForm.copy(
                    email = email,
                    emailError = null
                ),
                error = null
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { state ->
            state.copy(
                loginForm = state.loginForm.copy(
                    password = password,
                    passwordError = null
                ),
                error = null
            )
        }
    }

    fun login() {
        val currentState = _uiState.value
        val email = currentState.loginForm.email.trim()
        val password = currentState.loginForm.password

        if (!validateLoginForm(email, password)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            user = user,
                            isLoggedIn = true,
                            loginForm = LoginFormState()
                        ) 
                    }
                    _events.send(AuthEvent.NavigateToHome)
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Login failed"
                    _uiState.update { 
                        it.copy(isLoading = false, error = errorMessage) 
                    }
                    _events.send(AuthEvent.ShowError(errorMessage))
                }
        }
    }

    private fun validateLoginForm(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    loginForm = state.loginForm.copy(emailError = "Email is required")
                )
            }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { state ->
                state.copy(
                    loginForm = state.loginForm.copy(emailError = "Invalid email format")
                )
            }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    loginForm = state.loginForm.copy(passwordError = "Password is required")
                )
            }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { state ->
                state.copy(
                    loginForm = state.loginForm.copy(passwordError = "Password must be at least 6 characters")
                )
            }
            isValid = false
        }

        return isValid
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            logoutUseCase()
                .onSuccess {
                    _uiState.update { 
                        AuthUiState(isLoggedIn = false) 
                    }
                    _events.send(AuthEvent.NavigateToLogin)
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Logout failed"
                    _uiState.update { 
                        it.copy(isLoading = false, error = errorMessage) 
                    }
                    _events.send(AuthEvent.ShowError(errorMessage))
                }
        }
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { 
                        it.copy(user = user, isLoggedIn = true) 
                    }
                    _events.send(AuthEvent.NavigateToHome)
                }
                .onFailure {
                    _uiState.update { 
                        it.copy(isLoggedIn = false, user = null) 
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
