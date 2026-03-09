package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.game.GameConfig
import org.dmitriigliuz.linguatwist.game.GameEngine
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import kotlin.system.exitProcess
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary

class ConsoleApp(
    private val config: GameConfig,
    private val loader: VocabularyLoader,
    private val writer: VocabularyWriter,
    private val renderer: ConsoleRenderer,
    private val input: ConsoleInput,
    private val messages: Messages
) {
    fun run() {
        renderer.printLogo()
        renderer.printGreetings()
        while (true) {
            when (input.selectMenuItem(
                listOf("🎮 Start game", "🚪 Exit"),
                "\n📋 Main Menu 📋"
            )) {
                0 -> startGame()
                1 -> exitGame()
            }
        }
    }
    
    private fun exitGame() {
        renderer.printWarning("👋 Bye!")
        exitProcess(0)
    }

    private fun startGame() {
        renderer.emptyLine()
        val vocabularyLoadResult = renderer.withProgressMessage("Loading vocabulary...") {
            loader.load(config.spreadsheetPath).getOrThrow()
        }
        renderer.emptyLine()
        if (vocabularyLoadResult == null) {
            renderer.printError("Failed to load vocabulary")
            exitGame()
            return
        }
        var vocabulary = vocabularyLoadResult.vocabulary
        renderer.emptyLine()
        renderer.printVocabularyInfo(vocabulary.size, vocabularyLoadResult)
        renderer.emptyLine()
        do {
            val finalState = gameSession(vocabulary)
            val result = finalState.getResultOrNull()
            if (result == null) {
                renderer.printError("Failed to get result")
            }
            finalState.getUpdatedVocabularyOrNull()?.let { vocabulary = it }
        } while (input.promptPlayAgain())

        val saveResult = renderer.withProgressMessage("Saving scores to spreadsheet...") {
            writer.save(config.spreadsheetPath, vocabulary).getOrThrow()
        }
        if (saveResult == null) {
            renderer.printError("Failed to save scores")
        }
   }

    private fun gameSession(vocabulary: Vocabulary): GameState {
        val defaultNumberOfRounds = GameEngine.defaultNumberOfRounds(vocabulary, config)
        val totalRounds = input.promptNumberOfRounds(defaultNumberOfRounds, config.maxRounds)
        val engine = GameEngine(totalRounds, config)
        renderer.printMaxMistakesNumber(engine.maxMistakes)
        renderer.emptyLine()
        renderer.printLetsGo()
        renderer.emptyLine()
        var gameState: GameState = engine.startNewGame(vocabulary)
        var feedbackMessage = renderer.initialFeedback
        while (gameState is GameState.InProgress) {
            val displayState = gameState.toRoundDisplayState(
                engine.completionPercentage(gameState),
                feedbackMessage
            )
            renderer.updateRoundDisplay(displayState)
            when (val inputIndexesResult = input.promptIndexes()) {
                is IndexInput.Exit -> exitGame()
                is IndexInput.Invalid -> feedbackMessage = renderer.buildErrorFeedback()
                is IndexInput.Indexes -> {
                    val (leftIndex, rightIndex) = inputIndexesResult.pair
                    val indexes = Pair(leftIndex - 1, rightIndex - 1)
                    val (roundResult, newState) = engine.processAnswer(indexes, gameState)
                    gameState = newState
                    feedbackMessage = renderer.buildRoundResultFeedback(roundResult)
                }
            }
        }
        val result = gameState.getResultOrNull()
        result?.let {
            feedbackMessage = renderer.buildGameResult(it)
            val displayState = it.toDisplayState(feedbackMessage)
            renderer.updateRoundDisplay(displayState)
        }
        renderer.stopRoundDisplay()
        return gameState
    }
}