package org.dmitriigliuz.linguatwist.presentation.ui.emoji

class StandardEmojiProvider(private val useEmojis: Boolean): EmojiProvider {

    override fun get(type: Emoji): String = when {
        useEmojis -> type.unicode
        type.fallback.isNotEmpty() -> type.fallback
        else -> ""
    }

    override fun format(type: Emoji, text: String): String =
        "${get(type)} $text"
}