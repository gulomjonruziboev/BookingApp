package uz.buron.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.buron.R
import uz.buron.ui.components.FormPhoneField
import uz.buron.ui.components.FormTextField
import uz.buron.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        FormPhoneField(
            digits = uiState.phoneDigits,
            onDigitsChange = viewModel::onPhoneChange,
            label = stringResource(R.string.booking_phone),
            error = uiState.phoneError
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.field_password),
            error = uiState.passwordError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.login_submit),
            onClick = viewModel::login,
            enabled = !uiState.isLoading
        )

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_no_account))
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Muvaffaqiyatli ro'yxatdan o'tdingiz!")
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        FormTextField(
            value = uiState.firstName,
            onValueChange = viewModel::onFirstNameChange,
            label = stringResource(R.string.field_first_name),
            error = uiState.firstNameError
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = uiState.lastName,
            onValueChange = viewModel::onLastNameChange,
            label = stringResource(R.string.field_last_name),
            error = uiState.lastNameError
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormPhoneField(
            digits = uiState.phoneDigits,
            onDigitsChange = viewModel::onPhoneChange,
            label = stringResource(R.string.booking_phone),
            error = uiState.phoneError
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.field_password),
            error = uiState.passwordError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.register_submit),
            onClick = viewModel::register,
            enabled = !uiState.isLoading
        )

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_has_account))
        }
    }
}
