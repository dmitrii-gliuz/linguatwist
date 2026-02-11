package org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji

interface EmojiProvider {
    fun get(type: Emoji): String
    fun format(type: Emoji, text: String): String
}