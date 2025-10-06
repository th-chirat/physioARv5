// utils/LocaleManager.kt
package com.example.physioarv5.utils

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "settings")

object LocaleManager {
    private val KEY_LANG = stringPreferencesKey("app_language")

    fun languageFlow(ctx: Context): Flow<String> =
        ctx.dataStore.data.map { it[KEY_LANG] ?: "th" }

    suspend fun saveLanguage(ctx: Context, lang: String) {
        ctx.dataStore.edit { it[KEY_LANG] = lang }
    }

    fun applyAppLocale(ctx: Context, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val res = ctx.resources
        val config = Configuration(res.configuration).apply {
            setLocale(locale)
        }
        res.updateConfiguration(config, res.displayMetrics)
    }

    /** อ่านค่าที่บันทึกไว้แบบ blocking ใช้ตอนบูต Activity เพื่อให้ locale ถูกต้องตั้งแต่เฟรมแรก */
    fun getSavedLanguageBlocking(ctx: Context): String = runBlocking {
        ctx.dataStore.data.first()[KEY_LANG] ?: "th"
    }

    /** สะดวกใช้: apply ภาษาเดิมที่บันทึกไว้ทันที */
    fun applySavedLocaleBlocking(ctx: Context) {
        applyAppLocale(ctx, getSavedLanguageBlocking(ctx))
    }
}
