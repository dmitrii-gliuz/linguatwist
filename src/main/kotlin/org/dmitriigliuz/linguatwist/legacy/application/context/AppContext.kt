package org.dmitriigliuz.linguatwist.legacy.application.context

import org.dmitriigliuz.linguatwist.legacy.AppConfig
import org.dmitriigliuz.linguatwist.legacy.infrastructure.localization.LocalizationProvider
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.MessageBuilder
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji.EmojiProvider

data class AppContext(
    val config: AppConfig,
    val localizationProvider: LocalizationProvider,
    val emojiProvider: EmojiProvider,      // ← Новое поле
    val messageBuilder: MessageBuilder
){
    fun getString(key: String): String = localizationProvider.getString(key)

    fun isLanguageSupported(languageTag: String): Boolean =
        localizationProvider.isLanguageSupported(languageTag)

    fun isCurrentLanguageSupported() = isLanguageSupported(config.languageTag)

    fun getCurrentLanguage(): String = localizationProvider.getCurrentLanguage()

}