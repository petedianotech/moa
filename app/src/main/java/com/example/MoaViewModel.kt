package com.example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoaViewModel : ViewModel() {

    private val repository = AudioRepository.getInstance()

    val isRecording: StateFlow<Boolean> = repository.isRecording
    val amplitude: StateFlow<Float> = repository.amplitude
    val connectionStatus: StateFlow<AudioRepository.ConnectionStatus> = repository.connectionStatus

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _messageHistory = MutableStateFlow<List<MessageItem>>(emptyList())
    val messageHistory: StateFlow<List<MessageItem>> = _messageHistory.asStateFlow()

    init {
        // Retrieve and check API key status dynamically from the Secrets Gradle Plugin
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        val welcomeText = if (hasValidKey) {
            "moa is initialized with a secure Gemini API Key! Your massive intelligence is now completely active. Tap the microphone or enter text below to begin."
        } else {
            "Welcome to moa! 🚀 We noticed you haven't configured your custom Gemini API key yet. To run this app locally outside of Google AI Studio, please create a '.env' file in your project's root folder and append 'GEMINI_API_KEY=your_actual_key_here'."
        }

        // Pre-populate with a warm welcome dialog item
        _messageHistory.value = listOf(
            MessageItem(welcomeText, false)
        )
    }

    data class MessageItem(
        val text: String,
        val isUser: Boolean,
        val id: Long = System.currentTimeMillis()
    )

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun submitTextQuery(text: String) {
        if (text.trim().isEmpty()) return

        val userMsg = MessageItem(text, isUser = true)
        _messageHistory.value = _messageHistory.value + userMsg
        _inputText.value = ""

        // Non-blocking simulated Response for demonstration
        viewModelScope.launch {
            delay(1200)
            val aiMsg = MessageItem(
                text = "Understood. Analyzing request: \"$text\". Accessing specialized reasoning models in background...",
                isUser = false
            )
            _messageHistory.value = _messageHistory.value + aiMsg
        }
    }

    fun selectTaskPreset(preset: String) {
        _inputText.value = preset
    }

    fun clearHistory() {
        _messageHistory.value = listOf(
            MessageItem("Session history cleared. Start typing or tap voice input to engage moa Live assistant.", false)
        )
    }

    fun toggleVoiceInput(context: Context) {
        val intent = Intent(context, MoaAudioService::class.java)
        if (isRecording.value) {
            intent.action = MoaAudioService.ACTION_STOP
            context.startService(intent)
        } else {
            intent.action = MoaAudioService.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
