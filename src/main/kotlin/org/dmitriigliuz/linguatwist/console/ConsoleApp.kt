package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.terminal.Terminal
import org.dmitriigliuz.linguatwist.console.theming.Theme
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
    private val theme: Theme,
) {
    private val emoji = theme.emoji
    private val pendingSave = PendingSave()

    enum class AppState { MainMenu, Exit }

    companion object {
        fun create(
            coordinator: GameCoordinator,
            terminal: Terminal,
            theme: Theme,
            pairsPerRound: Int
        ): ConsoleApp {
            return ConsoleApp(
                coordinator = coordinator,
                input = ConsoleInput(terminal, theme, pairsPerRound),
                renderer = ConsoleRenderer(terminal, theme),
                theme = theme
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
        pendingSave.attemptSave { snapshot ->
            tryCreateBackup(snapshot).fold(
                onSuccess = {
                    runCatching {
                        renderer.emptyLine()
                        saveWithFeedback(snapshot)
                    }.getOrElse {
                        println("TUI rendering failed, using plain output")
                        fallbackSavePlain(snapshot)
                    }.onFailure { println("Failed to save scores") }
                },
                onFailure =  { error -> Result.failure(error) }
            )
        }
    }

    private fun fallbackSavePlain(snapshot: VocabularySnapshot): Result<Unit> {
        print("Saving scores... ")
        return coordinator.saveVocabulary(snapshot.path, snapshot.vocabulary).onSuccess {
            println("Done")
        }
    }

    private fun tryCreateBackup(snapshot: VocabularySnapshot) = runCatching {
        coordinator.createVocabularyBackup(snapshot.path)
    }.onFailure {
        runCatching { renderer.printError(Messages.file_error_unableToBackup()) }
            .onFailure {
                println(
                    "The app has crashed unexpectedly and was unable to back up the previous vocabulary. " +
                            "Unfortunately, your current progress will not be saved."
                )
            }
    }

    private fun printByeSafely() = runCatching { renderer.printBye() }.onFailure { println("Bye!") }

    private fun mainMenu() {
        var appState: AppState = AppState.MainMenu
        while (appState == AppState.MainMenu) {
            when (input.selectMenuItem(
                listOf(
                    emoji.startGame.prefixed(Messages.menu_main_start()),
                    emoji.exit.prefixed(Messages.menu_main_exit())
                ), "\n  » ${Messages.menu_main_title()} «"
            )) {
                0 -> appState = startGame()
                1 -> appState = AppState.Exit
            }
        }
    }

    private fun startGame(): AppState {
        pendingSave.reset()
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
        if (pendingSave.hasSnapshot() && input.promptSaveScores()) {
            pendingSave.attemptSave { saveWithFeedback(it) }
        } else pendingSave.preventFurtherSaveAttempts()
        return if (gameState is GameState.Aborted) AppState.Exit else AppState.MainMenu
    }

    private fun saveWithFeedback(snapshot: VocabularySnapshot): Result<Unit> {
        val (vocabularyPath, vocabulary) = snapshot
        val result = renderer.withProgressMessage(Messages.game_saving_scores()) {
            coordinator.saveVocabulary(vocabularyPath, vocabulary)
        }
        return result
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
        renderer.printStartingGameMessage()
        var gameState: GameState = engine.startNewGame(vocabulary)
        var feedback: Feedback = Feedback.Initial(Messages.game_feedback_initial())
        var displayState: GameDisplayState? = null
        while (gameState is GameState.InProgress) {
            displayState = gameState.toRoundDisplayState(feedback)
            renderer.updateGameDisplay(displayState)
            val inputIndexesResult = input.promptIndexes {
                renderer.updateGameDisplay(displayState!!.copy(userInput = it))
            }
            when (inputIndexesResult) {
                is IndexInput.Invalid -> feedback = renderer.buildInputErrorFeedback()
                is IndexInput.Exit -> {
                    displayState = displayState.confirmExit()
                    renderer.updateGameDisplay(displayState)
                    val exitConfirmed = input.promptConfirmExit {
                        renderer.updateGameDisplay(displayState!!.copy(userInput = it))
                    }
                    if (exitConfirmed) return GameState.Aborted
                }
                is IndexInput.Indexes -> {
                    val (leftIndex, rightIndex) = inputIndexesResult.pair
                    val indexes = Pair(leftIndex, rightIndex)
                    val (roundResult, newState) = engine.processAnswer(indexes, gameState)
                    gameState = newState
                    gameState.getUpdatedVocabularyOrNull()?.let {
                        pendingSave.update(VocabularySnapshot(vocabularyPath, it))
                    }
                    feedback = renderer.buildRoundResultFeedback(roundResult)
                }
            }
        }
        val result = coordinator.finishSession(gameState, startTime)
        feedback = if (result == null) renderer.noResultFeedback else renderer.buildGameResultFeedback(result)
        displayState = result?.toDisplayState(feedback) ?: displayState!!.copy(feedback = feedback)
        renderer.updateGameDisplay(displayState)
        return gameState
    }
}

internal class PendingSave {
    @Volatile
    private var vocabularySnapshot: VocabularySnapshot? = null
    private val savedOnce: AtomicBoolean = AtomicBoolean(false)

    internal fun attemptSave(saveOperation: (VocabularySnapshot) -> Result<Unit>): Result<Unit>? {
        val snapshot = tryClaimSaveRight() ?: return null
        return saveOperation(snapshot)
    }

    private fun tryClaimSaveRight(): VocabularySnapshot? {
        val snapshot = vocabularySnapshot
        return if (snapshot == null || !savedOnce.compareAndSet(false, true)) {
            null
        } else snapshot
    }

    internal fun preventFurtherSaveAttempts() {
        savedOnce.set(true)
    }

    fun hasSnapshot() = vocabularySnapshot != null

    fun reset() {
        vocabularySnapshot = null
        savedOnce.compareAndSet(true, false)
    }

    fun update(snapshot: VocabularySnapshot?) {
        vocabularySnapshot = snapshot
    }
}


internal data class VocabularySnapshot(val path: String, val vocabulary: Vocabulary)