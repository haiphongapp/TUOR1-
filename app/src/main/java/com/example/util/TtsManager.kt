package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(context: Context, onInitSuccess: () -> Unit = {}) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val viLocale = Locale("vi", "VN")
                val result = tts?.setLanguage(viLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default locale if Vietnamese is not available
                    tts?.language = Locale.getDefault()
                }
                tts?.setSpeechRate(0.95f) // Slightly clearer rate
                isInitialized = true
                onInitSuccess()
            } else {
                Log.e("TtsManager", "TextToSpeech initialization failed with status $status")
            }
        }
    }

    fun speakCodeAndStartRecording(
        orderCode: String,
        onSpeechFinished: () -> Unit
    ) {
        if (!isInitialized || tts == null) {
            // If TTS is not available, immediately callback
            onSpeechFinished()
            return
        }

        // Format order code for better speech clarity (e.g. "SPX 8 3 9 2" rather than concatenated gibberish)
        val formattedCode = formatCodeForSpeech(orderCode)
        val speechText = "Đã quét mã $formattedCode. Bắt đầu quay video."

        val utteranceId = "ORDER_SPEECH_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                onSpeechFinished()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechFinished()
            }
        })

        val result = tts?.speak(
            speechText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )

        if (result != TextToSpeech.SUCCESS) {
            // Fallback if speak failed
            onSpeechFinished()
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_GENERIC_${System.currentTimeMillis()}")
        }
    }

    private fun formatCodeForSpeech(code: String): String {
        val sb = StringBuilder()
        for (char in code) {
            if (char.isDigit()) {
                sb.append("$char ")
            } else if (char.isLetter()) {
                sb.append("${char.uppercaseChar()} ")
            } else {
                sb.append(" ")
            }
        }
        return sb.toString().trim()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
