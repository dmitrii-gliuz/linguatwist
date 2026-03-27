package org.dmitriigliuz.linguatwist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.console.ConsoleApp
import org.dmitriigliuz.linguatwist.console.CyberpunkTheme
import org.dmitriigliuz.linguatwist.console.DaylightTheme
import org.dmitriigliuz.linguatwist.game.GameConfig
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyLoader
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyWriter


class LinguaTwistCommand: CliktCommand(name = "linguatwist") {
    val file by option("-f", "--file", help = "Path to the vocabulary file")
        .default("vocabulary.xlsx")
    val lang by option("-l", "--lang", help = "Localization language tag (e.g. en, ru, de, etc.)")
        .default("en")
    val lightTheme by option("--light-theme",
        help = "Use light color theme optimized for light terminal backgrounds"
    ).flag(default = false)

    override fun run() {
        val colorTheme = if (!lightTheme) CyberpunkTheme else DaylightTheme
        val terminal = Terminal(theme = colorTheme.toTerminalTheme())
        Runtime.getRuntime().addShutdownHook(Thread { terminal.println("\n\n👋 Bye!") })
        val config = GameConfig(spreadsheetPath = file, languageTag = lang)
        val coordinator = GameCoordinator(
            config = config,
            loader = ExcelVocabularyLoader(),
            writer = ExcelVocabularyWriter(),
        )
        val consoleApp = ConsoleApp.create(coordinator, terminal, colorTheme)
        consoleApp.run()
    }
}

fun main(args: Array<String>) = LinguaTwistCommand().main(args)