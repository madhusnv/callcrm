package com.educonsult.crm.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educonsult.crm.ui.components.LoadingButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phone: String,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var otpValues by rememberSaveable { mutableStateOf(List(6) { "" }) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var resendCountdown by rememberSaveable { mutableIntStateOf(30) }
    var canResend by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val scope = rememberCoroutineScope()

    val maskedPhone = remember(phone) {
        if (phone.length >= 4) {
            "*".repeat(phone.length - 4) + phone.takeLast(4)
        } else {
            phone
        }
    }

    val otpComplete = remember(otpValues) { otpValues.all { it.isNotEmpty() } }

    LaunchedEffect(Unit) {
        while (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        }
        canResend = true
    }

    fun resendOtp() {
        scope.launch {
            canResend = false
            resendCountdown = 30
            snackbarHostState.showSnackbar("OTP resent successfully")
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
            canResend = true
        }
    }

    fun verifyOtp() {
        if (!otpComplete) return
        
        isLoading = true
        scope.launch {
            delay(1500)
            isLoading = false
            onVerificationSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
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
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Verification Code",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We have sent a verification code to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = maskedPhone,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                otpValues.forEachIndexed { index, value ->
                    OtpDigitField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                val newOtpValues = otpValues.toMutableList()
                                newOtpValues[index] = newValue
                                otpValues = newOtpValues

                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else if (newValue.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        },
                        focusRequester = focusRequesters[index],
                        onBackspace = {
                            if (value.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        enabled = !isLoading
                    )
                    if (index < 5) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive code?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = ::resendOtp,
                    enabled = canResend && !isLoading
                ) {
                    Text(
                        text = if (canResend) "Resend" else "Resend in ${resendCountdown}s"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            LoadingButton(
                text = "Verify",
                onClick = ::verifyOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                isLoading = isLoading,
                enabled = otpComplete
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun OtpDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty()) {
                onValueChange("")
                onBackspace()
            } else {
                onValueChange(newValue.takeLast(1))
            }
        },
        modifier = modifier
            .width(48.dp)
            .focusRequester(focusRequester),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        enabled = enabled
    )
}
