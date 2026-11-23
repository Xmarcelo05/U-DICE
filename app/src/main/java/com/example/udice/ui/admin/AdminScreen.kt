package com.example.udice.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.TipoReaccion
import com.example.udice.data.model.User

private sealed interface AdminAction {
    data object Create : AdminAction
    data class Reset(val account: User) : AdminAction
    data class Delete(val account: User) : AdminAction
    data object AddReaction : AdminAction
}

@Composable
fun AdminRoute(stateHolder: AdminStateHolder = hiltViewModel()) {
    val accounts by stateHolder.accounts.collectAsStateWithLifecycle()
    val reactions by stateHolder.reactionTypes.collectAsStateWithLifecycle()
    val operation by stateHolder.operation.collectAsStateWithLifecycle()
    var action by remember { mutableStateOf<AdminAction?>(null) }

    LaunchedEffect(operation.success) {
        if (operation.success != null) action = null
    }
    AdminScreen(accounts, reactions, operation, action == null, stateHolder::refresh) {
        stateHolder.clearResult()
        action = it
    }

    action?.let { current ->
        AdminActionDialog(
            action = current,
            operation = operation,
            onDismiss = {
                stateHolder.clearResult()
                action = null
            },
            onCreate = stateHolder::createAccount,
            onReset = stateHolder::resetPassword,
            onDelete = stateHolder::deleteAccount,
            onAddReaction = stateHolder::addReaction
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminScreen(
    accounts: List<User>,
    reactions: List<TipoReaccion>,
    operation: AdminOperation,
    showStatus: Boolean,
    onRefresh: () -> Unit,
    onAction: (AdminAction) -> Unit
) {
    var showingReactions by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!showingReactions, { showingReactions = false }, { Text("Facultades") })
                    FilterChip(showingReactions, { showingReactions = true }, { Text("Reacciones") })
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(if (showingReactions) AdminAction.AddReaction else AdminAction.Create) },
                        enabled = !operation.busy,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(if (showingReactions) "Añadir reacción" else "Crear cuenta ASO", Modifier.padding(start = 6.dp))
                    }
                    TextButton(onClick = onRefresh, enabled = !operation.busy) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text("Actualizar", Modifier.padding(start = 6.dp))
                    }
                }
            }
            if (showStatus) {
                if (operation.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                operation.error?.let { item { Text(it, color = colors.error) } }
                operation.success?.let { item { Text(it, color = colors.primary) } }
            }
            if (showingReactions) {
                items(reactions, key = { it.id }) { type ->
                    AdminCard {
                        Text("${type.emoji}  ${type.displayName}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                if (accounts.isEmpty()) item { Text("Todavía no hay cuentas de facultad.") }
                items(accounts, key = { it.id }) { user ->
                    AdminCard {
                        Text(user.nombre, style = MaterialTheme.typography.titleMedium)
                        user.facultad?.let { Text(it.displayName, color = colors.primary) }
                        Text(user.email, color = colors.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onAction(AdminAction.Reset(user)) }, enabled = !operation.busy) {
                                Icon(Icons.Default.Lock, contentDescription = null)
                                Text("Cambiar contraseña", Modifier.padding(start = 6.dp))
                            }
                            TextButton(
                                onClick = { onAction(AdminAction.Delete(user)) },
                                enabled = !operation.busy,
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                                Text("Eliminar", Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCard(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.outline),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminActionDialog(
    action: AdminAction,
    operation: AdminOperation,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Facultad) -> Unit,
    onReset: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onAddReaction: (String, String) -> Unit
) {
    var name by remember(action) { mutableStateOf("") }
    var email by remember(action) { mutableStateOf("") }
    var password by remember(action) { mutableStateOf("") }
    var emoji by remember(action) { mutableStateOf("") }
    var faculty by remember(action) { mutableStateOf<Facultad?>(null) }
    var expanded by remember(action) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val enabled = !operation.busy
    val deleting = action is AdminAction.Delete
    val title = when (action) {
        AdminAction.Create -> "Crear cuenta de facultad"
        is AdminAction.Reset -> "Cambiar contraseña"
        is AdminAction.Delete -> "Eliminar cuenta"
        AdminAction.AddReaction -> "Añadir reacción"
    }
    val valid = when (action) {
        AdminAction.Create -> name.isNotBlank() && email.isNotBlank() && password.isNotEmpty() && faculty != null
        is AdminAction.Reset -> password.isNotEmpty()
        is AdminAction.Delete -> true
        AdminAction.AddReaction -> name.isNotBlank() && emoji.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = { if (enabled) onDismiss() },
        title = { Text(title) },
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.surface,
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (action == AdminAction.Create || action == AdminAction.AddReaction) {
                    AdminField(name, { name = it }, "Nombre", enabled)
                }
                when (action) {
                    AdminAction.Create -> {
                        AdminField(email, { email = it }, "Correo", enabled, KeyboardType.Email)
                        ExposedDropdownMenuBox(expanded, { if (enabled) expanded = it }) {
                            OutlinedTextField(
                                value = faculty?.displayName.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                enabled = enabled,
                                label = { Text("Facultad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded, { expanded = false }) {
                                Facultad.entries.forEach { option ->
                                    DropdownMenuItem(text = { Text(option.displayName) }, onClick = {
                                        faculty = option
                                        expanded = false
                                    })
                                }
                            }
                        }
                    }
                    is AdminAction.Reset -> Text(action.account.email)
                    is AdminAction.Delete -> Text("¿Eliminar el acceso de ${action.account.email}? Sus avisos publicados seguirán visibles.")
                    AdminAction.AddReaction -> AdminField(emoji, { emoji = it }, "Emoji", enabled)
                }
                if (action == AdminAction.Create || action is AdminAction.Reset) {
                    OutlinedTextField(
                        password, { password = it }, Modifier.fillMaxWidth(),
                        enabled = enabled,
                        label = { Text(if (action is AdminAction.Reset) "Nueva contraseña" else "Contraseña") },
                        supportingText = { Text("De 8 a 128 caracteres") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                if (operation.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                operation.error?.let { Text(it, color = colors.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (action) {
                        AdminAction.Create -> faculty?.let { onCreate(name, email, password, it) }
                        is AdminAction.Reset -> onReset(action.account.id, password)
                        is AdminAction.Delete -> onDelete(action.account.id)
                        AdminAction.AddReaction -> onAddReaction(name, emoji)
                    }
                },
                enabled = enabled && valid,
                shape = RoundedCornerShape(12.dp),
                colors = if (deleting) ButtonDefaults.buttonColors(containerColor = colors.error)
                    else ButtonDefaults.buttonColors()
            ) { Text(if (deleting) "Eliminar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = enabled) { Text("Cancelar") } }
    )
}

@Composable
private fun AdminField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value, onChange, Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
