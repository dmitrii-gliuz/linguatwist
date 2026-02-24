package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt
import com.github.ajalt.mordant.terminal.prompt
import org.dmitriigliuz.linguatwist.i18n.Messages

class ConsoleInput(
    private val terminal: Terminal,
    private val messages: Messages
) {
    fun promptIndexes(): Result<Pair<Int, Int>> = runCatching {
//        TODO Figure out how this function works
         val input = terminal.prompt(Theme.promptStyle("Enter indexes for matching words: "))
             ?: throw IllegalStateException("exit")
        if (input == "exit") throw IllegalStateException("exit")
        val parts = input.split("\\s+".toRegex()). map { it.toInt() }
        parts[0] to parts[1]
    }

    fun promptNumberOfRounds(default: Int, max: Int): Int {
//        TODO Figure out how this function works
        return terminal.prompt(
            prompt = Theme.promptStyle("Enter the number of rounds (default: $default, max: $max): "),
            default = default,
            convert = { input ->
                val n = input.toIntOrNull()
                if (n == null && n in 1..max) ConversionResult.Valid(n)
                else ConversionResult.Invalid("1-$max")
            }
        ) ?: default
    }

    fun promptPlayAgain(): Boolean = YesNoPrompt(
//        TODO Figure out how this function works
        prompt = "Do you want to play again? ",
        terminal = terminal,
        default = true
    ).ask() ?: false

    fun selectMenuItem(items: List<String>, title: String): Int {
        val selected = terminal.interactiveSelectList {
            entries(items)
            title(title)
        }
        return items.indexOf(selected)
    }
}