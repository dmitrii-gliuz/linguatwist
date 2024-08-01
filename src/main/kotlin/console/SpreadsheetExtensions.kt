package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Spreadsheet
import org.dmitriigliuz.linguatwist.core.Vocabulary
import org.dmitriigliuz.linguatwist.core.localeString

fun Spreadsheet.writeScoresSafely(vocabulary: Vocabulary): Boolean {
    while (true) {
        try {
            writeScores(vocabulary)
            return true
        } catch (e: Exception) {
            input("${Emojis.Warning}${localeString("Error")}: ${e.message}.\n${localeString("EnterSkipOrExit")}:" )
                .also { if (it == skipCommand) return false }
        }
    }
}