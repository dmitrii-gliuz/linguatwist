package org.dmitriigliuz.linguatwist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.Terminal
import de.comahe.i18n4k.forLocaleTag
import de.comahe.i18n4k.i18n4k
import de.comahe.i18n4k.systemLocale
import de.comahe.i18n4k.config.I18n4kConfigDefault
import org.dmitriigliuz.linguatwist.console.ConsoleApp
import org.dmitriigliuz.linguatwist.console.CyberpunkTheme
import org.dmitriigliuz.linguatwist.console.DaylightTheme
import org.dmitriigliuz.linguatwist.console.DefaultEmoji
import org.dmitriigliuz.linguatwist.console.NoEmoji
import org.dmitriigliuz.linguatwist.game.GameConfig
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyLoader
import org.dmitriigliuz.linguatwist.storage.excel.ExcelVocabularyWriter

class LinguaTwistCommand: CliktCommand(name = "linguatwist") {
    val file by option("-f", "--file", help = Messages.cli_option_file_help())
        .default("vocabulary.xlsx")
    val lang by option("-l", "--lang", help = Messages.cli_option_lang_help())
        .default(systemLocale.language.ifBlank { "en" })
    val lightTheme by option("--light-theme",
        help = Messages.cli_option_lightTheme_help()
    ).flag(default = false)
    val noEmoji by option("--no-emoji",
        help = Messages.cli_option_noEmoji_help()
    ).flag(default = false)

    override fun run() {
        i18n4k = I18n4kConfigDefault().apply {
            locale = forLocaleTag(lang)
        }
        val colorTheme = if (!lightTheme) CyberpunkTheme else DaylightTheme
        val terminal = Terminal(theme = colorTheme.toTerminalTheme())
        val emojiSet = if (noEmoji) NoEmoji else DefaultEmoji

        val config = GameConfig(spreadsheetPath = file,     languageTag = lang)
        val coordinator = GameCoordinator(
            config = config,
            loader = ExcelVocabularyLoader(),
            writer = ExcelVocabularyWriter(),
        )
        val consoleApp = ConsoleApp.create(coordinator, terminal, colorTheme, emojiSet)
        consoleApp.run()
    }
}

fun main(args: Array<String>) = LinguaTwistCommand().main(args)
