package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt
import com.github.ajalt.mordant.terminal.prompt
import com.github.ajalt.mordant.input.InputReceiver
import com.github.ajalt.mordant.input.isCtrlC
import com.github.ajalt.mordant.input.receiveKeyEvents
import org.dmitriigliuz.linguatwist.console.theming.Theme
import org.dmitriigliuz.linguatwist.i18n.Messages

private fun String.isOneDigit() = length == 1 && this[0].isDigit()

class ConsoleInput(
    private val terminal: Terminal,
    private val theme: Theme,
    private val pairsPerRound: Int
) {
    companion object {
        private val validInputRegex = """^\d+\s+\d+$""".toRegex()
        private val digitsRegex = """\d+""".toRegex()
        private val spacesRegex = """\s+""".toRegex()
    }
    private val colorTheme = theme.colorTheme
    private val emoji = theme.emoji

    fun promptIndexes(onBufferChange: (String) -> Unit): IndexInput {
        val digitsInIndexAmount = pairsPerRound.toString().length

        val input = readLineRaw(
            { key, buffer ->
                val digitPressed = key.isOneDigit()
                val parts = buffer.split(" ")
                if (buffer.isEmpty()) {
                    digitPressed
                } else if (buffer.length < digitsInIndexAmount && parts.size == 1) {
                    digitPressed || key == " "
                } else if (buffer.matches(digitsRegex)) {
                    key == " "
                } else if (parts.size == 2 && parts.last().length < digitsInIndexAmount) {
                    digitPressed
                } else false
            },
            onBufferChange = onBufferChange
        ) ?: run { return IndexInput.Exit }
        return if (!input.matches(validInputRegex)) {
            IndexInput.Invalid
        } else {
            val parts = input.split(spacesRegex).map { it.toIntOrNull()?.let { number -> number - 1 } }
            if (parts.any { it == null }) {
                IndexInput.Invalid
            } else {
                val pair = parts[0]!! to parts[1]!!
                IndexInput.Indexes(pair)
            }

        }
    }

    fun promptConfirmExit(onBufferChange: (String) -> Unit): Boolean {
        val input = readLineRaw(
            { key, buffer ->
                buffer.isEmpty() && key.length == 1 && key.lowercase() in listOf("y", "n")
            },
            onBufferChange
        )
        return input?.lowercase() == "y"
    }

    fun promptVocabularyPath() = terminal.prompt(
        prompt = colorTheme.promptStyle(emoji.vocabularyPath.prefixed(Messages.prompt_vocabulary_path()))
    )

    fun promptNumberOfRounds(default: Int, max: Int): Int {
        return terminal.prompt(
            prompt = colorTheme.promptStyle(
                emoji.round.prefixed(Messages.prompt_rounds(default, max))
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

    fun promptSaveScores(): Boolean = YesNoPrompt(
        prompt = emoji.saveScores.prefixed(Messages.prompt_saveScores()),
        terminal = terminal,
        default = true
    ).ask() ?: false

    fun selectMenuItem(items: List<String>, title: String): Int {
        val selected = terminal.interactiveSelectList {
            entries(items)
            title(title)
            showInstructions(theme.showMenuInstructions)
        }
        return items.indexOf(selected)
    }

    private fun readLineRaw(
        keyPressHandlerCondition: (String, StringBuilder) ->  Boolean,
        onBufferChange: (String) -> Unit = {}
    ): String? {
        val buffer = StringBuilder()
        val result = terminal.receiveKeyEvents { event ->
            if (event.isCtrlC) InputReceiver.Status.Finished(null)
            else {
                when (event.key) {
                    "Enter" -> InputReceiver.Status.Finished(buffer.toString())
                    "Backspace" -> {
                        if (buffer.isNotEmpty()) {
                            buffer.deleteAt(buffer.length - 1)
                            onBufferChange(buffer.toString())
                        }
                        InputReceiver.Status.Continue
                    }
                    else -> {
                        if (keyPressHandlerCondition(event.key, buffer)) {
                            buffer.append(event.key)
                            onBufferChange(buffer.toString())
                        } else {
                            print("\u0007")
                            System.out.flush()
                        }
                        InputReceiver.Status.Continue
                    }
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