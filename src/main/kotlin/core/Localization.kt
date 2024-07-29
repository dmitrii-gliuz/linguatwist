package org.dmitriigliuz.linguatwist.core

import java.util.*

lateinit var languageTag: String

enum class Languages(val locale: String) {
    ENGLISH("en"),
    RUSSIAN("ru"),
    UKRAINIAN("uk"),
    BELARUSIAN("be"),
    SPANISH("es"),
    GERMAN("de"),
    GEORGIAN("ge"),
    ITALIAN("it"),
    POLISH("pl"),
    JAPANESE("ja"),
    CHINESE("zh")
}

fun getResourceBundle(languageTag: String): ResourceBundle {
    return ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageTag))
}

lateinit var resourceBundle: ResourceBundle

fun localeString(key: String): String = resourceBundle.getString(key)

fun checkIfLocaleAvailable(locale: String) = Languages.entries.any { it.locale == locale }