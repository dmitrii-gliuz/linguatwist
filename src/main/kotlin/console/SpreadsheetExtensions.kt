package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Spreadsheet
import org.dmitriigliuz.linguatwist.core.Vocabulary
import org.dmitriigliuz.linguatwist.core.localeString

fun Spreadsheet.writeScoresSafely(vocabulary: Vocabulary) {
    while (true) {
        try {
            writeScores(vocabulary)
            break
        } catch (e: Exception) {
            val action = input(
                "⚠️ ${localeString("Error")}: ${e.message}.\n" +
                        "${localeString("EnterSkipOrExit")}:"
            )
            if (action == skipCommand) break
        }
    }
}