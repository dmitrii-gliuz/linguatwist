package org.dmitriigliuz.linguatwist.application.context

import org.dmitriigliuz.linguatwist.AppConfig
import org.dmitriigliuz.linguatwist.infrastructure.localization.LocalizationProvider
import org.dmitriigliuz.linguatwist.presentation.ui.MessageBuilder
import org.dmitriigliuz.linguatwist.presentation.ui.emoji.EmojiProvider

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