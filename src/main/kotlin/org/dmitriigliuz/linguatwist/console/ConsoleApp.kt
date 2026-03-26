package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.game.GameCoordinator
import kotlin.system.exitProcess
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary

class ConsoleApp(
    private val coordinator: GameCoordinator,
    private val renderer: ConsoleRenderer,
    private val input: ConsoleInput,
) {
    fun run() {
        renderer.printLogo()
        renderer.printGreetings()
        mainMenu()
    }

    private fun mainMenu() {
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
    
    private fun exitGame(): Nothing {
        renderer.stopRoundDisplay()
        exitProcess(0)
    }

    private fun startGame() {
        renderer.emptyLine()
        var vocabulary: Vocabulary
        renderer.withProgressMessage(Messages.LOADING_VOCABULARY) { coordinator.loadVocabulary() }?.let {
            vocabulary = it.vocabulary
            renderer.emptyLine()
            renderer.printVocabularyInfo(it)
            renderer.emptyLine()
        } ?: run {
            exitGame()
        }
        do {
            gameSession(vocabulary).getUpdatedVocabularyOrNull()?.let { vocabulary = it }
        } while (input.promptPlayAgain())
        renderer.withProgressMessage(Messages.SAVING_SCORES) { coordinator.saveVocabulary(vocabulary) }
   } 

    private fun gameSession(vocabulary: Vocabulary): GameState {
        val defaultNumberOfRounds = coordinator.defaultNumberOfRounds(vocabulary)
        val totalRounds = input.promptNumberOfRounds(defaultNumberOfRounds, coordinator.maxRounds)
        val (engine, startTime) = coordinator.startSession(totalRounds)
        renderer.printMaxMistakesNumber(engine.maxMistakes)
        renderer.emptyLine()
        renderer.printLetsGo()
        renderer.emptyLine()
        var gameState: GameState = engine.startNewGame(vocabulary)
        var feedbackMessage = Messages.INITIAL_FEEDBACK
        var displayState: RoundDisplayState? = null
        while (gameState is GameState.InProgress) {
            displayState = gameState.toRoundDisplayState(feedbackMessage)
            renderer.updateRoundDisplay(displayState)
            when (val inputIndexesResult = input.promptIndexes()) {
                is IndexInput.Invalid -> feedbackMessage = renderer.buildInputErrorFeedback()
                is IndexInput.Exit -> exitGame()
                is IndexInput.Indexes -> {
                    val (leftIndex, rightIndex) = inputIndexesResult.pair
                    val indexes = Pair(leftIndex - 1, rightIndex - 1)
                    val (roundResult, newState) = engine.processAnswer(indexes, gameState)
                    gameState = newState
                    feedbackMessage = renderer.buildRoundResultFeedback(roundResult)
                }
            }
        }

        val result = coordinator.finishSession(gameState, startTime)
        feedbackMessage = if (result == null) renderer.noResultFeedback else renderer.buildGameResultOutput(result)
        displayState = result?.toDisplayState(feedbackMessage) ?: displayState!!.copy(feedbackMessage = feedbackMessage)
        renderer.updateRoundDisplay(displayState)
        renderer.stopRoundDisplay()
        return gameState
    }
}