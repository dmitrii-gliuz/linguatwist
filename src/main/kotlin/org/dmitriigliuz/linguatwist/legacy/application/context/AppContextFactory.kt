package org.dmitriigliuz.linguatwist.legacy.application.context

import org.dmitriigliuz.linguatwist.AppConfig
import org.dmitriigliuz.linguatwist.legacy.infrastructure.localization.ResourceBundleLocalizationProvider
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.MessageBuilder
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji.StandardEmojiProvider

object AppContextFactory {

    fun create(config: AppConfig): Result<AppContext> = runCatching {
        val validationErrors = config.validate()
        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException("Invalid config: ${validationErrors.joinToString(", ")}")
        }
        val localizationProvider = ResourceBundleLocalizationProvider(config.languageTag)
        if (!localizationProvider.isLanguageSupported(config.languageTag)) {
            println("Warning: Language '${config.languageTag}' is not supported, falling back to English")
        }

        val emojiProvider = StandardEmojiProvider(config.useEmojis)
        val messageBuilder = MessageBuilder(emojiProvider)
        AppContext(
            config = config,
            localizationProvider = localizationProvider,
            emojiProvider = emojiProvider,
            messageBuilder = messageBuilder
        )
    }
}