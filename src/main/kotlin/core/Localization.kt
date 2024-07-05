package org.dmitriigliuz.linguatwist.core

import java.util.*

var languageTag = "en"

fun getResourceBundle(languageTag: String): ResourceBundle {
    return ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageTag))
}

var resourceBundle = getResourceBundle(languageTag)

fun message(key: String): String = resourceBundle.getString(key)