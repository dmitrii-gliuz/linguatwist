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

class ConsoleInput(
    private val terminal: Terminal,
    private val colorTheme: ColorTheme
) {
    fun promptIndexes(): IndexInput {
        val input = readLineRaw() ?: run { return IndexInput.Exit }
        terminal.cursor.move {
            startOfLine()
            clearLine()
        }
        val correctRegex = """^\d+\s+\d+$""".toRegex()
        if (!correctRegex.matches(input)) return IndexInput.Invalid
        val parts = input.split("""\s+""".toRegex()). map { it.toIntOrNull() }
        if (parts.any { it == null }) return IndexInput.Invalid
        val pair = parts[0]!! to parts[1]!!

        return IndexInput.Indexes(pair)
    }

    fun promptVocabularyPath() = terminal.prompt(
            prompt = colorTheme.promptStyle("📚 ${Messages.prompt_vocabulary_path}")
        )

    fun promptNumberOfRounds(default: Int, max: Int): Int {
        return terminal.prompt(
            prompt = colorTheme.promptStyle("🎯 ${Messages.prompt_rounds(default, max)}"),
            default = default,
            convert = { input ->
                val n = input.toIntOrNull()
                if (n != null && n in 1..max) ConversionResult.Valid(n)
                else ConversionResult.Invalid(Messages.prompt_rounds_invalid(default, max))
            }
        ) ?: default
    }

    fun promptPlayAgain(): Boolean = YesNoPrompt(
        prompt = "🔄 ${Messages.prompt_playAgain} ",
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

    private fun readLineRaw(): String? {
        val buffer = StringBuilder()
        return terminal.receiveKeyEvents { event ->
            if (event.isCtrlC) return@receiveKeyEvents InputReceiver.Status.Finished(null)
            when (event.key) {
                "Enter" -> InputReceiver.Status.Finished(buffer.toString())
                "Backspace" -> {
                    if (buffer.isNotEmpty()) {
                        buffer.deleteAt(buffer.length - 1)
                        print("\b \b")
                    }
                    InputReceiver.Status.Continue
                }
                else -> {
                    if (event.key.length  == 1 && event.key[0].isDigit() || event.key == " ") {
                        buffer.append(event.key)
                        print(event.key)
                    }
                    InputReceiver.Status.Continue
                }
            }
        }
    }
}

sealed class IndexInput {
    data class Indexes(val pair: Pair<Int, Int>): IndexInput()
    object Invalid: IndexInput()
    object Exit: IndexInput()
}