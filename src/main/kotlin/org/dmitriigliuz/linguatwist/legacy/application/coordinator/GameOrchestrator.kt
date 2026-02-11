package org.dmitriigliuz.linguatwist.legacy.application.coordinator

import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext
import org.dmitriigliuz.linguatwist.game.GameEngine
import org.dmitriigliuz.linguatwist.legacy.Spreadsheet

class GameOrchestrator(
    val context: AppContext,
    spreadsheet: Spreadsheet
) {
    val vocabulary = spreadsheet.loadVocabulary()

    fun newGame(numberOfRounds: Int): GameEngine {
        return GameEngine(numberOfRounds, context)
    }
}