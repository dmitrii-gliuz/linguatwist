package org.dmitriigliuz.linguatwist.core

import java.util.*

fun getResourceBundle(languageTag: String): ResourceBundle {
    return ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageTag))
}

lateinit var resourceBundle: ResourceBundle

fun localeString(key: String): String = resourceBundle.getString(key)