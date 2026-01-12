package com.educonsult.crm.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.educonsult.crm.ui.components.LoadingButton
import com.educonsult.crm.ui.components.PasswordTextField

data class RegisterFormState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOtp: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var formState by rememberSaveable { mutableStateOf(RegisterFormState()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val isFormValid by remember {
        derivedStateOf {
            formState.firstName.isNotBlank() &&
            formState.lastName.isNotBlank() &&
            formState.email.isNotBlank() &&
            formState.phone.isNotBlank() &&
            formState.password.isNotBlank() &&
            formState.confirmPassword.isNotBlank() &&
            formState.acceptedTerms &&
            formState.firstNameError == null &&
            formState.lastNameError == null &&
            formState.emailError == null &&
            formState.phoneError == null &&
            formState.passwordError == null &&
            formState.confirmPasswordError == null
        }
    }

    fun validateAndSubmit() {
        var hasError = false
        var updatedState = formState

        if (formState.firstName.isBlank()) {
            updatedState = updatedState.copy(firstNameError = "First name is required")
            hasError = true
        }
        if (formState.lastName.isBlank()) {
            updatedState = updatedState.copy(lastNameError = "Last name is required")
            hasError = true
        }
        if (formState.email.isBlank()) {
            updatedState = updatedState.copy(emailError = "Email is required")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email).matches()) {
            updatedState = updatedState.copy(emailError = "Invalid email format")
            hasError = true
        }
        if (formState.phone.isBlank()) {
            updatedState = updatedState.copy(phoneError = "Phone is required")
            hasError = true
        } else if (formState.phone.length < 10) {
            updatedState = updatedState.copy(phoneError = "Invalid phone number")
            hasError = true
        }
        if (formState.password.isBlank()) {
            updatedState = updatedState.copy(passwordError = "Password is required")
            hasError = true
        } else if (formState.password.length < 6) {
            updatedState = updatedState.copy(passwordError = "Password must be at least 6 characters")
            hasError = true
        }
        if (formState.confirmPassword != formState.password) {
            updatedState = updatedState.copy(confirmPasswordError = "Passwords do not match")
            hasError = true
        }

        formState = updatedState

        if (!hasError) {
            isLoading = true
            onNavigateToOtp(formState.phone)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join EduConsult CRM",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your account to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.firstName,
                    onValueChange = {
                        formState = formState.copy(firstName = it, firstNameError = null)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("First Name") },
                    singleLine = true,
                    isError = formState.firstNameError != null,
                    supportingText = formState.firstNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    ),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = formState.lastName,
                    onValueChange = {
                        formState = formState.copy(lastName = it, lastNameError = null)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Last Name") },
                    singleLine = true,
                    isError = formState.lastNameError != null,
                    supportingText = formState.lastNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.email,
                onValueChange = {
                    formState = formState.copy(email = it, emailError = null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                placeholder = { Text("Enter email address") },
                singleLine = true,
                isError = formState.emailError != null,
                supportingText = formState.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.phone,
                onValueChange = {
                    formState = formState.copy(phone = it, phoneError = null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number") },
                placeholder = { Text("Enter phone number") },
                singleLine = true,
                isError = formState.phoneError != null,
                supportingText = formState.phoneError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = formState.password,
                onValueChange = {
                    formState = formState.copy(password = it, passwordError = null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = "Password",
                placeholder = "Create a password",
                isError = formState.passwordError != null,
                errorMessage = formState.passwordError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = formState.confirmPassword,
                onValueChange = {
                    formState = formState.copy(confirmPassword = it, confirmPasswordError = null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = "Confirm Password",
                placeholder = "Re-enter password",
                isError = formState.confirmPasswordError != null,
                errorMessage = formState.confirmPasswordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        validateAndSubmit()
                    }
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = formState.acceptedTerms,
                    onCheckedChange = {
                        formState = formState.copy(acceptedTerms = it)
                    },
                    enabled = !isLoading
                )
                Text(
                    text = "I agree to the Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoadingButton(
                text = "Register",
                onClick = ::validateAndSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                isLoading = isLoading,
                enabled = isFormValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !isLoading
                ) {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
