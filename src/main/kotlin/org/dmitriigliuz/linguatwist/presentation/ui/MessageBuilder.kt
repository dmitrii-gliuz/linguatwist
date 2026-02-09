package org.dmitriigliuz.linguatwist.presentation.ui

import org.dmitriigliuz.linguatwist.presentation.ui.emoji.EmojiProvider
import org.dmitriigliuz.linguatwist.presentation.ui.emoji.Emoji

class MessageBuilder(private val emojiProvider: EmojiProvider) {

    // Main messages
    fun welcome(text: String): String = emojiProvider.format(Emoji.WELCOME, text)
    fun success(text: String): String = emojiProvider.format(Emoji.SUCCESS, text)
    fun error(text: String): String = emojiProvider.format(Emoji.ERROR, text)
    fun warning(text: String): String = emojiProvider.format(Emoji.WARNING, text)
    fun bye(text: String): String = emojiProvider.format(Emoji.BYE, text)

    // Game messages
    fun score(points: Int): String = emojiProvider.format(Emoji.SCORE, "Score: $points")
    fun mistakes(count: Int): String = emojiProvider.format(Emoji.MISTAKES, "Mistakes: $count")
    fun round(number: Int): String = emojiProvider.format(Emoji.ROUNDS, "Round $number")

    // Results and actions
    fun right(text: String): String = emojiProvider.format(Emoji.RIGHT, text)
    fun wrong(text: String): String = emojiProvider.format(Emoji.WRONG, text)

    // Progress and loading
    fun progressBar(progress: Int, total: Int, length: Int = 20): String {
        val percentage = if (total > 0) progress * 100 / total else 0
        val filled = (progress * length / total).coerceIn(0, length)
        val empty = length - filled

        val progressChar = emojiProvider.get(Emoji.PROGRESS).trim()
        val emptyChar = emojiProvider.get(Emoji.PROGRESS_EMPTY).trim()

        val bar = progressChar.repeat(filled) + emptyChar.repeat(empty)

        return if (progressChar.isNotEmpty() || emptyChar.isNotEmpty()) {
            "$bar $percentage%"
        } else {
            "[${"█".repeat(filled)}${"░".repeat(empty)}] $percentage%"
        }
    }
}