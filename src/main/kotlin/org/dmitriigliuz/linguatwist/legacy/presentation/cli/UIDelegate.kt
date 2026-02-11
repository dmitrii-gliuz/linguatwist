package org.dmitriigliuz.linguatwist.legacy.presentation.cli

import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji.Emoji

class UIDelegate(private val context: AppContext) {
    fun text(key: String): String = context.localizationProvider.getString(key)

    // Emoji shortcuts
    fun emoji(type: Emoji): String = context.emojiProvider.get(type)
    fun format(type: Emoji, text: String): String = context.emojiProvider.format(type, text)
    fun emojiText(type: Emoji, key: String): String =
        context.emojiProvider.format(type, text(key))

    // Message shortcuts
    fun welcome(text: String): String = context.messageBuilder.welcome(text(text))
    fun success(text: String): String = context.messageBuilder.success(text)
    fun error(text: String): String = context.messageBuilder.error(text)
    fun warning(text: String): String = context.messageBuilder.warning(text)
    fun bye(text: String): String = context.messageBuilder.bye(text(text))
}