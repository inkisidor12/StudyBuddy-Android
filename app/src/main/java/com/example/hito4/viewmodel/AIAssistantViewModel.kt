package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.BuildConfig
import com.example.hito4.data.dao.ChatDao
import com.example.hito4.data.entity.ChatMessage
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AIUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val educationLevel: String = ""
)

class AIAssistantViewModel(
    private val chatDao: ChatDao,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AIUiState())
    val ui: StateFlow<AIUiState> = _ui.asStateFlow()

    init {
        loadMessages()
        loadUserProfile()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val uid = userRepository.getCurrentUserProfile()?.uid ?: ""
            chatDao.getAllMessages(uid).collect { messages ->
                _ui.update { it.copy(messages = messages) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = userRepository.getCurrentUserProfile()
            _ui.update { it.copy(educationLevel = profile?.educationLevel ?: "") }
        }
    }

    fun onInputChange(text: String) = _ui.update { it.copy(inputText = text) }

    fun sendMessage() {
        val text = _ui.value.inputText.trim()
        if (text.isBlank() || _ui.value.isLoading) return

        viewModelScope.launch {
            val uid = userRepository.getCurrentUserProfile()?.uid ?: ""
            val userMsg = ChatMessage(role = "user", content = text, uid = uid)
            chatDao.insert(userMsg)

            val historial = _ui.value.messages.toMutableList()
            historial.add(userMsg)

            _ui.update { it.copy(inputText = "", isLoading = true, error = null) }

            try {
                val response = callClaude(historial, _ui.value.educationLevel)
                val assistantMsg = ChatMessage(role = "assistant", content = response, uid = uid)
                chatDao.insert(assistantMsg)
            } catch (e: Exception) {
                android.util.Log.e("AIAssistant", "Error: ${e.message}", e)
                _ui.update { it.copy(error = e.message ?: "Error al conectar con el asistente") }
            } finally {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            val uid = userRepository.getCurrentUserProfile()?.uid ?: ""
            chatDao.clearAll(uid)
        }
    }

    private suspend fun callClaude(messages: List<ChatMessage>, educationLevel: String): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = URL("https://api.anthropic.com/v1/messages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            connection.setRequestProperty("anthropic-version", "2023-06-01")
            connection.doOutput = true

            val systemPrompt = buildSystemPrompt(educationLevel)

            val messagesArray = JSONArray()
            val messagesToSend = messages.takeLast(20)
            messagesToSend.forEach { msg ->
                val obj = JSONObject()
                obj.put("role", msg.role)
                obj.put("content", msg.content)
                messagesArray.put(obj)
            }

            android.util.Log.d("AIAssistant", "Enviando ${messagesArray.length()} mensajes")

            val body = JSONObject()
            body.put("model", "claude-haiku-4-5")
            body.put("max_tokens", 1024)
            body.put("system", systemPrompt)
            body.put("messages", messagesArray)

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(body.toString())
            writer.flush()

            val responseCode = connection.responseCode
            val stream = if (responseCode == 200) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().readText()

            if (responseCode != 200) {
                throw Exception("Error API: $responseCode - $response")
            }

            val jsonResponse = JSONObject(response)
            jsonResponse.getJSONArray("content")
                .getJSONObject(0)
                .getString("text")
        }
    }

    private fun buildSystemPrompt(educationLevel: String): String {
        val nivel = if (educationLevel.isBlank()) "estudiante" else "estudiante de $educationLevel"

        return """
            Eres StudyBot, un asistente de estudio personal para un $nivel.
            
            Tu objetivo es GUIAR al estudiante, no darle las respuestas directamente.
            
            Reglas:
            - Nunca des la respuesta directa a un ejercicio o examen
            - Usa el método socrático: haz preguntas que lleven al estudiante a descubrir la respuesta
            - Si el estudiante está bloqueado, da pistas progresivas
            - Adapta el lenguaje y los ejemplos al nivel de $educationLevel
            - Cuando expliques conceptos, usa ejemplos concretos y cotidianos
            - Si el estudiante acierta, refuerza positivamente y profundiza
            - Sé cercano, motivador y paciente
            - Responde siempre en español
            - Mantén respuestas concisas (máximo 3-4 párrafos)
        """.trimIndent()
    }
}

class AIAssistantViewModelFactory(
    private val chatDao: ChatDao,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIAssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIAssistantViewModel(chatDao, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}