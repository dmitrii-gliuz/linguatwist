package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.game.GameCoordinator
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import java.util.concurrent.atomic.AtomicBoolean


class ConsoleApp private constructor(
    private val coordinator: GameCoordinator,
    private val input: ConsoleInput,
    private val renderer: ConsoleRenderer,
    private val emoji: EmojiSet
) {
    @Volatile
    private var currentVocabularySnapshot: Pair<String, Vocabulary>? = null
    private val savedOnce: AtomicBoolean = AtomicBoolean(false)
    enum class AppState { MainMenu, Exit }

    companion object {
        fun create(
            coordinator: GameCoordinator,
            terminal: Terminal,
            colorTheme: ColorTheme,
            emoji: EmojiSet,
            pairsPerRound: Int
        ): ConsoleApp {
            return ConsoleApp(
                coordinator = coordinator,
                input = ConsoleInput(terminal, colorTheme, emoji, pairsPerRound),
                renderer = ConsoleRenderer(terminal, colorTheme, emoji),
                emoji = emoji
            )
        }
    }

    fun run() {
        Runtime.getRuntime().addShutdownHook(Thread {
            saveOnShutdownIfNeeded()
            printByeSafely()
        })
        renderer.printLogo()
        renderer.printGreetings()
        mainMenu()
    }

    private fun saveOnShutdownIfNeeded() {
        val snapshot = tryClaimSaveRight() ?: return
        val (vocabularyPath, vocabulary) = snapshot
        if (tryCreateBackup(vocabularyPath).isFailure) return
        val saveResult = runCatching {
            renderer.emptyLine()
            saveWithFeedback(snapshot)
        }.getOrElse  {
                println("TUI rendering failed, using plain output")
                fallbackSavePlain(vocabularyPath, vocabulary)
            }
        saveResult.onFailure { println("Failed to save scores") }
    }

    private fun fallbackSavePlain(vocabularyPath: String, vocabulary: Vocabulary): Result<Unit> {
        print("Saving scores... ")
        return coordinator.saveVocabulary(vocabularyPath, vocabulary).onSuccess {
            println("Done")
        }
    }

    private fun tryCreateBackup(path: String) = runCatching { coordinator.createVocabularyBackup(path) }
        .onFailure {
            runCatching { renderer.printError(Messages.file_error_unableToBackup()) }
                .onFailure { println("The app has crashed unexpectedly and was unable to back up the previous vocabulary. " +
                        "Unfortunately, your current progress will not be saved.") }
        }

    private fun printByeSafely() = runCatching { renderer.printBye() }.onFailure { println("Bye!") }

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
        currentVocabularySnapshot = null
        savedOnce.compareAndSet(true, false)
        renderer.emptyLine()
        val vocabularyLoadResult = loadVocabularyWithRetry() ?: return AppState.Exit
        var vocabulary = vocabularyLoadResult.vocabulary
        val vocabularyPath = vocabularyLoadResult.path
        var gameState: GameState?
        do {
            gameState = gameSession(vocabularyPath, vocabulary).also { state ->
                state.getUpdatedVocabularyOrNull()?.let { vocabulary = it }
            }
            renderer.stopRoundDisplay()
        } while (gameState !is GameState.Aborted && input.promptPlayAgain())
        if (currentVocabularySnapshot != null && input.promptSaveScores()) {
            tryClaimSaveRight()?.let { saveWithFeedback(it) }
        } else savedOnce.set(true)
        return if (gameState is GameState.Aborted) AppState.Exit else AppState.MainMenu
    }

    private fun saveWithFeedback(snapshot: Pair<String, Vocabulary>): Result<Unit> {
        val (vocabularyPath, vocabulary) = snapshot
        val result = renderer.withProgressMessage(Messages.game_saving_scores()) {
            coordinator.saveVocabulary(vocabularyPath, vocabulary)
        }
        return result
    }


    private fun tryClaimSaveRight(): Pair<String, Vocabulary>? {
        val snapshot = currentVocabularySnapshot ?: return null
        if (!savedOnce.compareAndSet(false, true)) return null
        return snapshot
    }


    private fun loadVocabularyWithRetry(): VocabularyLoadResult? {
        var customSpreadsheetPath: String? = null
        while (true) {
            val result = renderer.withProgressMessage(Messages.game_loading_vocabulary()) {
                coordinator.loadVocabulary(customSpreadsheetPath)
            }
            if (result.isSuccess){
                val vocabularyLoadResult = result.getOrThrow()
                renderer.printVocabularyInfo(vocabularyLoadResult)
                renderer.emptyLine()
                return vocabularyLoadResult
            } else  {
                customSpreadsheetPath = input.promptVocabularyPath()?.ifBlank { null } ?: return null
            }
        }
    }

    private fun gameSession(vocabularyPath: String, vocabulary: Vocabulary): GameState {
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
                is IndexInput.Exit -> {
                    val confirmExitDisplayState = displayState.copy(promptType = RoundDisplayPrompt.CONFIRM_EXIT)
                    renderer.updateRoundDisplay(confirmExitDisplayState)
                    if (input.promptConfirmExit()) return GameState.Aborted
                }
                is IndexInput.Indexes -> {
                    val (leftIndex, rightIndex) = inputIndexesResult.pair
                    val indexes = Pair(leftIndex - 1, rightIndex - 1)
                    val (roundResult, newState) = engine.processAnswer(indexes, gameState)
                    gameState = newState
                    gameState.getUpdatedVocabularyOrNull()?.let {
                        currentVocabularySnapshot = vocabularyPath to it
                    }
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
