// File: utils/TextToSpeechHelper.kt
package com.example.physioarv5.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(
    context: Context,
    private val initialLangTag: String = "th",
    private val preferEngine: String? = "com.google.android.tts",
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var currentTag: String = initialLangTag

    init {
        tts = TextToSpeech(context) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) applyLanguage(initialLangTag)
        }.apply {
            preferEngine?.let { this?.setEngineByPackageName(it) }
        }
    }

    private fun toLocale(tag: String): Locale = when (tag.lowercase()) {
        "th", "th-th", "th_th" -> Locale("th", "TH")
        "en", "en-us", "en_us" -> Locale.US
        "en-gb", "en_uk"       -> Locale.UK
        else                   -> Locale.forLanguageTag(tag)
    }

    private fun applyLanguage(tag: String) {
        val loc = toLocale(tag)
        val res = tts?.setLanguage(loc)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            val fallback = if (tag.startsWith("en", true)) Locale.US else Locale("th", "TH")
            tts?.setLanguage(fallback)
        }
        // เลือก voice ให้ตรง locale ถ้ามี
        val v = tts?.voices?.firstOrNull { it.locale == loc && !it.isNetworkConnectionRequired }
        if (v != null) tts?.voice = v
        currentTag = tag
    }

    /** เรียกตอนผู้ใช้สลับภาษา */
    fun updateLanguage(tag: String) {
        if (!isReady) return
        if (!tag.equals(currentTag, true)) applyLanguage(tag)
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isReady || text.isBlank()) return
        val q = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, q, null, System.currentTimeMillis().toString())
    }

    /** อ่าน feedback โดยจะแปลเป็น EN ถ้าภาษาปัจจุบันคืออังกฤษ */
    fun speakFeedback(raw: String, langTag: String) {
        if (raw.isBlank()) return
        val text = if (langTag.startsWith("en", true)) {
            toEnglish(raw)
        } else raw
        speak(text)
    }

    /** แมปข้อความไทยยอดฮิต -> อังกฤษ (ชั่วคราว) */
    private fun toEnglish(raw: String): String = when (raw.trim()) {
        "หลังตรงครับ/ค่ะ", "หลังตรงค่ะ", "หลังตรง" -> "Keep your back straight."
        "ยกแขนให้สูงขึ้นอีกนิด"                    -> "Raise your arm a little higher."
        "ทำช้าๆ"                                   -> "Move slowly."
        "เหยียดเข่าให้ตรง"                          -> "Straighten your knee."
        "พักก่อนนะ"                                 -> "Take a short rest."
        else                                        -> raw // fallback
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isReady = false
    }
}
