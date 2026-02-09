package org.dmitriigliuz.linguatwist.infrastructure.localization

import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

class ResourceBundleLocalizationProvider(private val languageTag: String): LocalizationProvider {

    private val resourceBundle: ResourceBundle by lazy {
        try {
            ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageTag))
        } catch (_: Exception) {
            // Fallback to English if language not found
            ResourceBundle.getBundle("messages", Locale.forLanguageTag("en"))
        }
    }

    override fun getString(key: String): String = try {
        resourceBundle.getString(key)
    } catch (_: MissingResourceException) {
        "!$key!"
    }

    override fun isLanguageSupported(languageTag: String) = supportedLanguages.contains(languageTag)

    override fun getCurrentLanguage(): String = languageTag

    override fun getSupportedLanguages(): Set<String> = supportedLanguages

    companion object {
        private val supportedLanguages = setOf(
            "en", "ru", "uk", "be", "es", "de", "ge", "it", "pl", "ja", "zh"
        )
    }

}