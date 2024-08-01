package org.dmitriigliuz.linguatwist

import org.dmitriigliuz.linguatwist.console.consoleGame
import org.dmitriigliuz.linguatwist.console.useEmojis
import org.dmitriigliuz.linguatwist.core.getResourceBundle
import org.dmitriigliuz.linguatwist.core.languageTag
import org.dmitriigliuz.linguatwist.core.resourceBundle
import org.dmitriigliuz.linguatwist.core.spreadsheetPath


enum class Arguments(private val long: String, private val short: String, private val defaultValue: String) {
    SPREADSHEET("--spreadsheet", "-s", "vocabulary.xlsx"),
    LOCALE("--language", "-l", "en"),
    NO_EMOJIS("--no-emojis", "-n", "false");

    fun isPresent(args: Array<String>): Boolean {
        return args.contains(long) || args.contains(short)
    }

    fun getValue(args: Array<String>): String {
        val argIndex = args.indexOf(long)

        return if (argIndex != -1) {
            args[argIndex + 1]
        } else {
            val shortArgIndex = args.indexOf(short)
            if (shortArgIndex != -1) {
                args[shortArgIndex + 1]
            } else {
                defaultValue
            }
        }
    }
}


fun main(args: Array<String>) {
    spreadsheetPath = Arguments.SPREADSHEET.getValue(args)
    languageTag = Arguments.LOCALE.getValue(args).also { resourceBundle = getResourceBundle(it) }
    if (Arguments.NO_EMOJIS.isPresent(args)) { useEmojis = false}
    consoleGame()
}