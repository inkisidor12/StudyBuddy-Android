package com.example.hito4.screens


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.RegisterStep
import com.example.hito4.viewmodel.RegisterViewModel
import com.example.hito4.viewmodel.RegisterViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val vm: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(
            container.authRepository,
            container.userRepository
        )
    )
    val state by vm.ui.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onRegisterSuccess()
    }

    val stepIndex = when (state.step) {
        RegisterStep.PERSONAL -> 0
        RegisterStep.ACCOUNT -> 1
        RegisterStep.PASSWORD -> 2
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.step == RegisterStep.PERSONAL) onBack()
                        else vm.prevStep()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Barra de progreso
            LinearProgressIndicator(
                progress = { (stepIndex + 1) / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Paso ${stepIndex + 1} de 3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    when (state.step) {
                        RegisterStep.PERSONAL -> "Datos personales"
                        RegisterStep.ACCOUNT -> "Tu cuenta"
                        RegisterStep.PASSWORD -> "Contraseña"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
                label = "registerStep"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        RegisterStep.PERSONAL -> Step1Personal(vm, state.fullName, state.birthDate, state.phone)
                        RegisterStep.ACCOUNT -> Step2Account(vm, state.email, state.nickname, state.nicknameAvailable, state.checkingNickname)
                        RegisterStep.PASSWORD -> Step3Password(vm, state.password, state.confirmPassword, state.acceptedTerms)
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { vm.nextStep() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (state.step == RegisterStep.PASSWORD) "Registrarse" else "Siguiente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1Personal(
    vm: RegisterViewModel,
    fullName: String,
    birthDate: String,
    phone: String
) {
    Text("Datos personales", style = MaterialTheme.typography.titleLarge)
    Text("Cuéntanos quién eres", style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

    OutlinedTextField(
        value = fullName,
        onValueChange = { vm.onFullNameChange(it) },
        label = { Text("Nombre y apellidos") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = birthDate,
        onValueChange = { vm.onBirthDateChange(it) },
        label = { Text("Fecha de nacimiento (DD/MM/AAAA)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("01/01/2000") }
    )

    OutlinedTextField(
        value = phone,
        onValueChange = { vm.onPhoneChange(it) },
        label = { Text("Teléfono") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("+34 600 000 000") }
    )
}

@Composable
private fun Step2Account(
    vm: RegisterViewModel,
    email: String,
    nickname: String,
    nicknameAvailable: Boolean?,
    checkingNickname: Boolean
) {
    Text("Tu cuenta", style = MaterialTheme.typography.titleLarge)
    Text("Elige cómo quieres que te vean", style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

    OutlinedTextField(
        value = email,
        onValueChange = { vm.onEmailChange(it) },
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Column {
        OutlinedTextField(
            value = nickname,
            onValueChange = { vm.onNicknameChange(it) },
            label = { Text("Nickname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("PepeDestroyer") },
            trailingIcon = {
                when {
                    checkingNickname -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    nicknameAvailable == true -> Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.tertiary)
                    nicknameAvailable == false -> Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(Modifier.height(6.dp))

        OutlinedButton(
            onClick = { vm.checkNickname() },
            enabled = nickname.length >= 3 && !checkingNickname,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comprobar disponibilidad")
        }

        if (nicknameAvailable == true) {
            Text("✅ Nickname disponible", color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall)
        } else if (nicknameAvailable == false) {
            Text("❌ Nickname no disponible", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Step3Password(
    vm: RegisterViewModel,
    password: String,
    confirmPassword: String,
    acceptedTerms: Boolean
) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Text("Contraseña", style = MaterialTheme.typography.titleLarge)
    Text("Elige una contraseña segura", style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

    OutlinedTextField(
        value = password,
        onValueChange = { vm.onPasswordChange(it) },
        label = { Text("Contraseña") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { showPassword = !showPassword }) {
                Text(if (showPassword) "Ocultar" else "Ver")
            }
        }
    )

    // Indicador de seguridad
    if (password.isNotEmpty()) {
        val strength = vm.passwordStrength(password)
        val (label, color) = when (strength) {
            0, 1 -> "Débil" to MaterialTheme.colorScheme.error
            2 -> "Media" to MaterialTheme.colorScheme.tertiary
            3 -> "Fuerte" to MaterialTheme.colorScheme.secondary
            else -> "Muy fuerte" to MaterialTheme.colorScheme.primary
        }
        Column {
            LinearProgressIndicator(
                progress = { strength / 4f },
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { vm.onConfirmPasswordChange(it) },
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { showConfirm = !showConfirm }) {
                Text(if (showConfirm) "Ocultar" else "Ver")
            }
        },
        isError = confirmPassword.isNotEmpty() && password != confirmPassword
    )

    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
        Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = acceptedTerms,
            onCheckedChange = { vm.onAcceptedTermsChange(it) }
        )
        Text("Acepto los términos y condiciones", style = MaterialTheme.typography.bodyMedium)
    }
}