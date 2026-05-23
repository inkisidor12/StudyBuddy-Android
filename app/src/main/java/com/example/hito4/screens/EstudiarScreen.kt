package com.example.hito4.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.TreeBadge
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.FocusViewModelV2
import com.example.hito4.viewmodel.FocusViewModelV2Factory
import com.example.hito4.viewmodel.SubjectsViewModel
import com.example.hito4.viewmodel.SubjectsViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudiarScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val context = LocalContext.current

    val subjectsVm: SubjectsViewModel = viewModel(
        factory = SubjectsViewModelFactory(container.subjectRepository)
    )
    val focusVm: FocusViewModelV2 = viewModel(
        factory = FocusViewModelV2Factory(
            container.subjectRepository,
            container.studySessionRepository,
            container.userRepository
        )
    )

    val subjects by subjectsVm.subjects.collectAsState()
    val focusState by focusVm.ui.collectAsState()

    var selectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var showNewSubjectDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }

    if (focusState.showShareDialog && focusState.selectedSubject != null) {
        SessionShareDialog(
            nickname = focusState.nickname,
            subjectName = focusState.selectedSubject!!.name,
            minutes = focusState.lastSessionMinutes,
            context = context,
            onDismiss = { focusVm.dismissShareDialog() }
        )
    }

    if (selectedSubject != null) {
        FocusTimerContent(
            vm = focusVm,
            subject = selectedSubject!!,
            onBack = {
                focusVm.reset()
                selectedSubject = null
            },
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewSubjectDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Mis asignaturas", style = MaterialTheme.typography.titleLarge)

                if (subjects.isEmpty()) {
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Aún no tienes asignaturas.")
                        Text("Pulsa + para crear la primera.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(subjects) { s ->
                            Card(
                                onClick = {
                                    focusVm.selectSubject(s)
                                    selectedSubject = s
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📚", style = MaterialTheme.typography.titleLarge)
                                        Column {
                                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Pulsa para estudiar",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { subjectsVm.delete(s) },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) { Text("Borrar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewSubjectDialog = false },
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            textContentColor = MaterialTheme.colorScheme.onPrimary,
            title = { Text("Nueva asignatura") },
            text = {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        cursorColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        subjectsVm.add(newSubject)
                        newSubject = ""
                        showNewSubjectDialog = false
                    },
                    enabled = newSubject.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Crear") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNewSubjectDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    )
                ) { Text("Cancelar") }
            }
        )
    }
}

// ===================== DIALOG DE COMPARTIR =====================
@Composable
fun SessionShareDialog(
    nickname: String,
    subjectName: String,
    minutes: Int,
    context: Context,
    onDismiss: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val date = remember {
        SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date())
    }
    val treeEmoji = when {
        minutes >= 60 -> "🌲"
        minutes >= 30 -> "🌳"
        minutes >= 15 -> "🌿"
        else -> "🌱"
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "¡Sesión completada! 🎉",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Tarjeta que se convertirá en PNG
            Box(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B5E20),
                                Color(0xFF2E7D32),
                                Color(0xFF43A047)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(treeEmoji, fontSize = 64.sp)
                    Text(
                        "StudyBuddy 🌲",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    Text(
                        "@$nickname",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱️", fontSize = 20.sp)
                        Text(
                            "$minutes minutos",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFA5D6A7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            saveBitmapToGallery(context, bitmap, nickname, subjectName)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar")
                }
                Button(
                    onClick = {
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            shareBitmap(context, bitmap, nickname, subjectName, minutes)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Compartir")
                }
            }

            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

private fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    nickname: String,
    subjectName: String
): Boolean {
    return try {
        val filename = "StudyBuddy_${nickname}_${subjectName}_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/StudyBuddy")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "StudyBuddy")
            dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        true
    } catch (e: Exception) {
        false
    }
}

private fun shareBitmap(
    context: Context,
    bitmap: Bitmap,
    nickname: String,
    subjectName: String,
    minutes: Int
) {
    try {
        val filename = "StudyBuddy_share_${System.currentTimeMillis()}.png"
        val cachefile = File(context.cacheDir, filename)
        FileOutputStream(cachefile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cachefile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "🌱 Acabo de estudiar $minutes minutos de $subjectName con StudyBuddy! ¿Te unes? #StudyBuddy #Estudiar")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir sesión"))
    } catch (e: Exception) {
        android.util.Log.e("Share", "Error al compartir: ${e.message}")
    }
}

// ===================== TIMER =====================
@Composable
private fun FocusTimerContent(
    vm: FocusViewModelV2,
    subject: SubjectEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.ui.collectAsState()

    val rawProgress =
        if (state.totalSeconds == 0) 0f
        else 1f - (state.remainingSeconds.toFloat() / state.totalSeconds.toFloat())

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 500),
        label = "progressAnimation"
    )

    val timeText = formatSeconds(state.remainingSeconds)

    val growthText = when {
        animatedProgress < 0.25f -> "Semilla 🌱"
        animatedProgress < 0.60f -> "Brote 🌿"
        animatedProgress < 0.95f -> "Árbol 🌳"
        else -> "Bosque 🌲"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                subject.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ForestCard(modifier = Modifier.fillMaxWidth()) {
            Text("Minutos planificados: ${state.plannedMinutes}", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.changePlannedMinutes(-5) }, enabled = !state.isRunning) { Text("-5") }
                Button(onClick = { vm.changePlannedMinutes(+5) }, enabled = !state.isRunning) { Text("+5") }
                OutlinedButton(onClick = { vm.setPlannedMinutes(25) }, enabled = !state.isRunning) { Text("25") }
            }
        }

        ForestCard(modifier = Modifier.fillMaxWidth()) {
            val progressColor = when {
                animatedProgress < 0.25f -> MaterialTheme.colorScheme.onPrimary
                animatedProgress < 0.60f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(150.dp),
                    strokeWidth = 10.dp,
                    color = progressColor
                )
                Text(timeText, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                AnimatedContent(
                    targetState = growthText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) +
                                scaleIn(initialScale = 0.8f, animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "treeStage"
                ) { text ->
                    val emoji = when (text) {
                        "Semilla 🌱" -> "🌱"
                        "Brote 🌿" -> "🌿"
                        "Árbol 🌳" -> "🌳"
                        else -> "🌲"
                    }
                    TreeBadge(emoji = emoji, label = text)
                }
                if (state.isFinished) {
                    AnimatedVisibility(
                        visible = state.isFinished,
                        enter = fadeIn() + scaleIn(animationSpec = spring())
                    ) {
                        Text("¡Sesión completada y guardada! 🎉", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.start() }, modifier = Modifier.weight(1f), enabled = !state.isRunning && state.remainingSeconds > 0) { Text("Empezar") }
            OutlinedButton(onClick = { vm.pause() }, modifier = Modifier.weight(1f), enabled = state.isRunning) { Text("Pausar") }
        }

        OutlinedButton(onClick = { vm.reset() }, modifier = Modifier.fillMaxWidth()) { Text("Reset") }
    }
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}