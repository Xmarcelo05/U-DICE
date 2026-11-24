package com.example.udice.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.udice.data.model.Facultad

@Composable
fun AuthScreen(
    state: AuthFormState,
    onShowRegistration: (Boolean) -> Unit,
    onDismissVerification: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, Facultad, String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    state.verificationEmail?.let { address ->
        AlertDialog(
            onDismissRequest = onDismissVerification,
            title = { Text("Revisa tu correo") },
            text = { Text("Enviamos un enlace de verificación a $address. Ábrelo antes de iniciar sesión. Si no lo encuentras, revisa la carpeta de correo no deseado.") },
            confirmButton = { TextButton(onClick = onDismissVerification) { Text("Entendido") } }
        )
    }
    BackHandler(state.registering && !state.busy) { onShowRegistration(false) }

    if (state.registering) {
        RegisterScreen(state, email, { email = it }, onRegister) { onShowRegistration(false) }
    } else {
        LoginScreen(state, email, { email = it }, onLogin) { onShowRegistration(true) }
    }
}

@Composable
internal fun AuthMessage(state: AuthFormState) {
    state.message?.let { message ->
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}
