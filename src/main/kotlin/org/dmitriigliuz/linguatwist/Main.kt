package org.dmitriigliuz.linguatwist

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.console.ConsoleApp
import org.dmitriigliuz.linguatwist.console.ConsoleInput
import org.dmitriigliuz.linguatwist.console.ConsoleRenderer
import org.dmitriigliuz.linguatwist.game.GameConfig
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyLoader
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyWriter

fun main(args: Array<String>) {
    val terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR)
    val messages = Messages("en")
    val consoleApp = ConsoleApp(
        config = GameConfig(),
        loader = ExcelVocabularyLoader(),
        writer = ExcelVocabularyWriter(),
        renderer = ConsoleRenderer(terminal, messages),
        input = ConsoleInput(terminal, messages),
        messages = messages
    )
    consoleApp.run()
}