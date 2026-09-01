package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.example.data.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Initializing : VoiceState()
    object Listening : VoiceState()
    data class Processing(val recognizedText: String) : VoiceState()
    data class Success(val recognizedText: String, val message: String) : VoiceState()
    data class Error(val errorMessage: String) : VoiceState()
}

class VoiceSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val hindiLocale = Locale("hi", "IN")
                    val result = textToSpeech?.setLanguage(hindiLocale)
                    isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    fun speak(text: String, language: Language = Language.HINDI) {
        if (!isTtsReady || textToSpeech == null) return
        try {
            val locale = if (language == Language.HINDI) Locale("hi", "IN") else Locale.ENGLISH
            textToSpeech?.language = locale
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VoiceFeedbackId")
        } catch (_: Exception) {
            // Ignore TTS errors gracefully
        }
    }

    fun startListening(language: Language = Language.HINDI) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Error(
                if (language == Language.HINDI)
                    "माइक उपलब्ध नहीं है। कृपया नीचे दिए गए उदाहरण चुनें या लिखकर जोड़ें।"
                else
                    "Speech recognition not available on this device. Use preset voice chips or text."
            )
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener(language))
            }

            val localeStr = if (language == Language.HINDI) "hi-IN" else "en-IN"
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeStr)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeStr)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == Language.HINDI) "बोलिए (उदा. 1 किलो आलू, 2 पैकेट दूध)" else "Speak grocery items (e.g., 1 kg potato, 2 packet milk)")
            }

            _voiceState.value = VoiceState.Listening
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error(e.localizedMessage ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        if (_voiceState.value is VoiceState.Listening) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun simulateVoiceInput(phrase: String) {
        _voiceState.value = VoiceState.Processing(phrase)
        onSpeechRecognized?.invoke(phrase)
    }

    fun resetState() {
        stopListening()
        _voiceState.value = VoiceState.Idle
    }

    private fun createListener(language: Language) = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = VoiceState.Listening
        }

        override fun onBeginningOfSpeech() {
            _voiceState.value = VoiceState.Listening
        }

        override fun onRmsChanged(rmsdB: Float) {
            _rmsDb.value = rmsdB
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _voiceState.value = VoiceState.Processing("आवाज समझी जा रही है...")
        }

        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH ->
                    if (language == Language.HINDI) "आवाज समझ नहीं आई, कृपया फिर से बोलें" else "No match found. Please try again."
                SpeechRecognizer.ERROR_NETWORK ->
                    if (language == Language.HINDI) "इंटरनेट कनेक्शन जांचें" else "Network error. Check your connection."
                SpeechRecognizer.ERROR_AUDIO ->
                    if (language == Language.HINDI) "माइक रिकॉर्डिंग त्रुटि" else "Audio recording error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    if (language == Language.HINDI) "कोई आवाज नहीं सुनाई दी" else "No speech detected"
                else ->
                    if (language == Language.HINDI) "कृपया दोबारा बोलें" else "Could not recognize speech"
            }
            _voiceState.value = VoiceState.Error(errorMsg)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: ""
            if (recognizedText.isNotBlank()) {
                _voiceState.value = VoiceState.Processing(recognizedText)
                onSpeechRecognized?.invoke(recognizedText)
            } else {
                _voiceState.value = VoiceState.Error(
                    if (language == Language.HINDI) "कोई शब्द नहीं मिला" else "No text recognized"
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _voiceState.value = VoiceState.Processing(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun release() {
        stopListening()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
    }
}
