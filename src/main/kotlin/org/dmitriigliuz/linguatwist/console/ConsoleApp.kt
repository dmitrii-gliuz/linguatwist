package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult

class ConsoleApp private constructor(
    private val coordinator: GameCoordinator,
    private val input: ConsoleInput,
    private val renderer: ConsoleRenderer,
    private val emoji: EmojiSet
) {
    enum class AppState { MainMenu, Exit }

    companion object {
        fun create(
            coordinator: GameCoordinator,
            terminal: Terminal,
            colorTheme: ColorTheme,
            emoji: EmojiSet
        ): ConsoleApp {
            return ConsoleApp(
                coordinator = coordinator,
                input = ConsoleInput(terminal, colorTheme, emoji),
                renderer = ConsoleRenderer(terminal, colorTheme, emoji),
                emoji = emoji
            )
        }
    }

    fun run() {
        renderer.printLogo()
        renderer.printGreetings()
        mainMenu()
    }

    private fun mainMenu() {
        var appState: AppState = AppState.MainMenu
        while (appState == AppState.MainMenu) {
            when (input.selectMenuItem(
                listOf(
                    emoji.startGame.prefixed(Messages.menu_main_start()),
                    emoji.exit.prefixed(Messages.menu_main_exit())
                ), "\n  » ${Messages.menu_main_title} «"
            )) {
                0 -> appState = startGame()
                1 -> appState = AppState.Exit
            }
        }
    }

    private fun startGame(): AppState {
        renderer.emptyLine()
        val vocabularyLoadResult = loadVocabularyWithRetry() ?: return AppState.Exit
        var vocabulary = vocabularyLoadResult.vocabulary
        val vocabularyPath = vocabularyLoadResult.path
        var gameState: GameState?
        do {
            gameState = gameSession(vocabulary).also { state ->
                state.getUpdatedVocabularyOrNull()?.let { vocabulary = it }
            }
            renderer.stopRoundDisplay()
        } while (gameState !is GameState.Aborted && input.promptPlayAgain())
        renderer.withProgressMessage(Messages.game_saving_scores()) {
            coordinator.saveVocabulary(vocabularyPath, vocabulary)
        }
        return if (gameState is GameState.Aborted) AppState.Exit else AppState.MainMenu
    }

    private fun loadVocabularyWithRetry(): VocabularyLoadResult? {
        var customSpreadsheetPath: String? = null
        while (true) {
            renderer.withProgressMessage(Messages.game_loading_vocabulary()) {
                coordinator.loadVocabulary(customSpreadsheetPath)
            }?.let {
                renderer.printVocabularyInfo(it)
                renderer.emptyLine()
                return it
            } ?: run { customSpreadsheetPath = input.promptVocabularyPath()?.ifBlank { null } ?: return null }
        }
    }

    private fun gameSession(vocabulary: Vocabulary): GameState {
        val defaultNumberOfRounds = coordinator.defaultNumberOfRounds(vocabulary)
        val totalRounds = input.promptNumberOfRounds(defaultNumberOfRounds, coordinator.maxRounds)
        val (engine, startTime) = coordinator.startSession(totalRounds)
        renderer.printAllowedMistakesNumber(engine.allowedMistakes)
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
                is IndexInput.Exit -> return GameState.Aborted
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
        return gameState
    }
}
