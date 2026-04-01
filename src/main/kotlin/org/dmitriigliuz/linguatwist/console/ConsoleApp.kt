package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.i18n.Messages
import kotlin.system.exitProcess
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary

class ConsoleApp private constructor(
    private val coordinator: GameCoordinator,
    private val input: ConsoleInput,
    private val renderer: ConsoleRenderer,
) {
    companion object {
        fun create(coordinator: GameCoordinator, terminal: Terminal, colorTheme: ColorTheme): ConsoleApp {
            return ConsoleApp(
                coordinator = coordinator,
                input = ConsoleInput(terminal, colorTheme),
                renderer = ConsoleRenderer(terminal, colorTheme),
            )
        }
    }

    fun run() {
        renderer.printLogo()
        renderer.printGreetings()
        mainMenu()
    }

    private fun mainMenu() {
        while (true) {
            when (input.selectMenuItem(
                listOf("🎮 ${Messages.menu_main_start}", "🚪 ${Messages.menu_main_exit}"),
                "\n  » ${Messages.menu_main_title} «"
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
        var vocabulary: Vocabulary = loadVocabularyWithRetry()
        do {
            gameSession(vocabulary).getUpdatedVocabularyOrNull()?.let { vocabulary = it }
        } while (input.promptPlayAgain())
        renderer.withProgressMessage(Messages.game_saving_scores()) { coordinator.saveVocabulary(vocabulary) }
    }

    private fun loadVocabularyWithRetry(): Vocabulary {
        var customSpreadsheetPath: String? = null
        while (true) {
            renderer.withProgressMessage(Messages.game_loading_vocabulary()) {
                coordinator.loadVocabulary(customSpreadsheetPath)
            }?.let {
                renderer.emptyLine()
                renderer.printVocabularyInfo(it)
                renderer.emptyLine()
                return it.vocabulary
            } ?: run {
                customSpreadsheetPath = input.promptVocabularyPath()?.ifBlank { null } ?: exitGame()
            }
        }
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
        var feedbackMessage = Messages.game_feedback_initial()
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