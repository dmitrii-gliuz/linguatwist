package org.dmitriigliuz.linguatwist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.console.ConsoleApp
import org.dmitriigliuz.linguatwist.console.ConsoleInput
import org.dmitriigliuz.linguatwist.console.ConsoleRenderer
import org.dmitriigliuz.linguatwist.game.GameConfig
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyLoader
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyWriter


class LinguaTwistCommand: CliktCommand(name = "linguatwist") {
    init {
        context { terminal = Terminal()}
    }
    val file by option("-f", "--file", help = "Path to the vocabulary file")
        .default("vocabulary.xlsx")
    val lang by option("-l", "--lang", help = "Localization language tag (e.g. en, ru, de, etc.)")
        .default("en")

    override fun run() {
        val terminal = currentContext.terminal
        Runtime.getRuntime().addShutdownHook(Thread { terminal.println("\n\n👋 Bye!") })
        val config = GameConfig(spreadsheetPath = file, languageTag = lang)
        val coordinator = GameCoordinator(
            config = config,
            loader = ExcelVocabularyLoader(),
            writer = ExcelVocabularyWriter(),
        )
        val consoleApp = ConsoleApp(
            coordinator = coordinator,
            renderer = ConsoleRenderer(terminal),
            input = ConsoleInput(terminal),
        )
        consoleApp.run()
    }
}

fun main(args: Array<String>) = LinguaTwistCommand().main(args)