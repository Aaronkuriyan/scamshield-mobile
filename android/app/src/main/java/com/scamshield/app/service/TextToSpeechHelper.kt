package com.scamshield.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langCode = com.scamshield.app.util.LocaleHelper.getLanguage(context)
            val locale = when (langCode) {
                "kn" -> Locale("kn", "IN")
                "hi" -> Locale("hi", "IN")
                else -> Locale.ENGLISH
            }
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "Language $locale not fully supported on this TTS engine, falling back to default.")
                tts?.setLanguage(Locale.ENGLISH)
            }
            isInitialized = true
            tts?.setSpeechRate(0.9f) // Slightly slower rate for elderly clarity
            tts?.setPitch(1.0f)
        } else {
            Log.e("TTS", "TextToSpeech initialization failed.")
        }
    }

    fun speakWarning(warningText: String) {
        if (!isVoiceAlertEnabled(context)) return

        if (isInitialized && tts != null) {
            val spokenMessage = when (com.scamshield.app.util.LocaleHelper.getLanguage(context)) {
                "kn" -> "ಎಚ್ಚರಿಕೆ! $warningText"
                "hi" -> "सावधान! $warningText"
                else -> "Warning. This message may be a scam. $warningText"
            }
            tts?.speak(spokenMessage, TextToSpeech.QUEUE_FLUSH, null, "scamshield_voice_alert")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        private const val PREFS_NAME = "scamshield_prefs"
        private const val KEY_TTS_ENABLED = "tts_voice_warnings_enabled"

        fun isVoiceAlertEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_TTS_ENABLED, true) // Enabled by default for elderly safety
        }

        fun setVoiceAlertEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
        }
    }
}
