package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt
import com.github.ajalt.mordant.terminal.prompt
import com.github.ajalt.mordant.input.InputReceiver
import com.github.ajalt.mordant.input.isCtrlC
import com.github.ajalt.mordant.input.receiveKeyEvents
import org.dmitriigliuz.linguatwist.i18n.Messages

private fun String.isOneDigit() = length == 1 && this[0].isDigit()

class ConsoleInput(
    private val terminal: Terminal,
    private val colorTheme: ColorTheme,
    private val emoji: EmojiSet,
    private val pairsPerRound: Int
) {
    fun promptIndexes(): IndexInput {
        val digitsInIndexAmount = pairsPerRound.toString().length
        val digitsRegex = Regex("""\d+""")

        val input = readLineRaw { key, buffer ->
            val parts = buffer.split(" ")
            if (buffer.isEmpty()) {
                key.isOneDigit()
            } else if (buffer.length < digitsInIndexAmount && parts.size == 1) {
                key.isOneDigit() || key == " "
            } else if (buffer.matches(digitsRegex)) {
                key == " "
            } else if (parts.size == 2 && parts.last().length < digitsInIndexAmount) {
                key.isOneDigit()
            } else false
        } ?: run { return IndexInput.Exit }
        val correctRegex = """^\d+\s+\d+$""".toRegex()
        if (!correctRegex.matches(input)) return IndexInput.Invalid
        val parts = input.split("""\s+""".toRegex()).map { it.toIntOrNull() }
        if (parts.any { it == null }) return IndexInput.Invalid
        val pair = parts[0]!! to parts[1]!!
        return IndexInput.Indexes(pair)
    }

    fun promptConfirmExit(): Boolean {
        val input = readLineRaw { key, buffer ->
            buffer.isEmpty() && key.length == 1 && key.lowercase() in listOf("y", "n")
        }
        return input?.lowercase() == "y"
    }

    fun promptVocabularyPath() = terminal.prompt(
        prompt = colorTheme.promptStyle(emoji.vocabularyPath.prefixed(Messages.prompt_vocabulary_path()))
    )

    fun promptNumberOfRounds(default: Int, max: Int): Int {
        return terminal.prompt(
            prompt = colorTheme.promptStyle(
                emoji.numberOfRounds.prefixed(Messages.prompt_rounds(default, max))
            ),
            default = default,
            convert = { input ->
                val n = input.toIntOrNull()
                if (n != null && n in 1..max) ConversionResult.Valid(n)
                else ConversionResult.Invalid(Messages.prompt_rounds_invalid(default, max))
            }
        ) ?: default
    }

    fun promptPlayAgain(): Boolean = YesNoPrompt(
        prompt = emoji.playAgain.prefixed(Messages.prompt_playAgain()),
        terminal = terminal,
        default = true
    ).ask() ?: false

    fun selectMenuItem(items: List<String>, title: String): Int {
        val selected = terminal.interactiveSelectList {
            entries(items)
            title(title)
            showInstructions(false)
        }
        return items.indexOf(selected)
    }

    private fun readLineRaw(keyPressCondition: (String, StringBuilder) -> Boolean): String? {
        val buffer = StringBuilder()
        val result = terminal.receiveKeyEvents { event ->
            if (event.isCtrlC) InputReceiver.Status.Finished(null)
            else when (event.key) {
                "Enter" -> InputReceiver.Status.Finished(buffer.toString())
                "Backspace" -> {
                    if (buffer.isNotEmpty()) {
                        buffer.deleteAt(buffer.length - 1)
                        print("\b \b")
                    }
                    InputReceiver.Status.Continue
                }
                else -> {
                    if (keyPressCondition(event.key, buffer)) {
                        buffer.append(event.key)
                        print(event.key)
                    } else {
                        print("\u0007")
                        System.out.flush()
                    }
                    InputReceiver.Status.Continue
                }
            }
        }
        terminal.cursor.move {
            startOfLine()
            clearLine()
        }
        return result
    }
}

sealed class IndexInput {
    data class Indexes(val pair: Pair<Int, Int>) : IndexInput()
    object Invalid : IndexInput()
    object Exit : IndexInput()
}