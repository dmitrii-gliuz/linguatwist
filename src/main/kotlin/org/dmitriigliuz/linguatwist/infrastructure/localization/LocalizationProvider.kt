package org.dmitriigliuz.linguatwist.infrastructure.localization

interface LocalizationProvider {
    fun getString(key: String): String
    fun isLanguageSupported(languageTag: String): Boolean
    fun getCurrentLanguage(): String
    fun getSupportedLanguages(): Set<String>
}