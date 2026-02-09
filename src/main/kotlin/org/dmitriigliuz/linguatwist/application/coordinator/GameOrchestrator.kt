package org.dmitriigliuz.linguatwist.application.coordinator

import org.dmitriigliuz.linguatwist.application.context.AppContext
import org.dmitriigliuz.linguatwist.domain.engine.GameEngine
import org.dmitriigliuz.linguatwist.infrastructure.storage.legacy.Spreadsheet

class GameOrchestrator(
    val context: AppContext,
    spreadsheet: Spreadsheet
) {
    val vocabulary = spreadsheet.loadVocabulary()

    fun newGame(numberOfRounds: Int): GameEngine {
        return GameEngine(numberOfRounds, context)
    }
}