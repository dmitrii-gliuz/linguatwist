package org.dmitriigliuz.linguatwist.i18n

import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

class Messages(languageTag: String) {
//    private val bundle = ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageTag))
    private val bundle = null

//    fun get(key: String): String = try { bundle.getString(key) } catch (_: MissingResourceException) { "!$key!" }
    fun get(key: String) = null
}