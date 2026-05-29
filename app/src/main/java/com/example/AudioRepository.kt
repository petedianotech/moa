package com.example

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

/**
 * Clean architecture repository managing real-time audio streams,
 * status states, and connection hooks for Gemini Live API bidirectional integration.
 */
class AudioRepository private constructor() {

    companion object {
        @Volatile
        private var instance: AudioRepository? = null

        fun getInstance(): AudioRepository {
            return instance ?: synchronized(this) {
                instance ?: AudioRepository().also { instance = it }
            }
        }
    }

    private val _audioStream = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    /**
     * Stream of raw PCM chunks captured from the microphone.
     * Ready to be transmitted immediately to Gemini Live via WebSockets.
     */
    val audioStream: SharedFlow<ByteArray> = _audioStream.asSharedFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    /**
     * Direct live amplitude level normalized (0.0 to 1.0) to drive screen visualizers & bubbles.
     */
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    fun setRecordingState(active: Boolean) {
        _isRecording.value = active
        if (!active) {
            _amplitude.value = 0f
        }
    }

    fun updateAmplitude(amp: Float) {
        _amplitude.value = amp
    }

    suspend fun emitAudioChunk(chunk: ByteArray) {
        _audioStream.emit(chunk)
        // Here is where the connection class would listen to this flow and
        // pipeline raw bytes up to Gemini Live servers, e.g.:
        // webSocketSession.send(Frame.Binary(chunk))
    }

    fun connectToGeminiLive() {
        _connectionStatus.value = ConnectionStatus.CONNECTING
        Log.d("AudioRepository", "Starting bidirectional session to Gemini Live...")
        // Simulate handshake delay or status transition
        _connectionStatus.value = ConnectionStatus.CONNECTED
    }

    fun disconnectFromGeminiLive() {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        Log.d("AudioRepository", "Stopped bidirectional connection to Gemini Live.")
    }
}
