package com.example.taskprioritizer.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.taskprioritizer.ui.common.FeedbackCard
import com.example.taskprioritizer.ui.common.LoadingState

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onModeChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("TaskPrioritizer", style = MaterialTheme.typography.headlineLarge)
            Text("Organiza tus pendientes y decide por dónde empezar.")
            Text(if (state.isRegistering) "Crear cuenta" else "Iniciar sesión", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.email.isNotEmpty() && state.emailError != null,
                supportingText = { if (state.email.isNotEmpty()) state.emailError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !state.isSubmitting,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.password.isNotEmpty() && state.passwordError != null,
                supportingText = { if (state.password.isNotEmpty()) state.passwordError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.isRegistering) {
                OutlinedTextField(
                    value = state.confirmation,
                    onValueChange = onConfirmationChange,
                    label = { Text("Confirmar contraseña") },
                    enabled = !state.isSubmitting,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.confirmation.isNotEmpty() && state.confirmationError != null,
                    supportingText = { if (state.confirmation.isNotEmpty()) state.confirmationError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            state.error?.let { FeedbackCard(it, isError = true) }
            if (state.isSubmitting) LoadingState()
            Button(onClick = onSubmit, enabled = state.isValid && !state.isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isRegistering) "Crear cuenta" else "Entrar")
            }
            TextButton(onClick = onModeChange, enabled = !state.isSubmitting) {
                Text(if (state.isRegistering) "Ya tengo cuenta" else "No tengo cuenta: registrarme")
            }
            Text("La primera vez necesitas internet. Después podrás consultar los datos guardados sin conexión.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
