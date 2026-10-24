package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.github.ajalt.mordant.terminal.success
import com.github.ajalt.mordant.terminal.warning
import com.github.ajalt.mordant.widgets.*
import org.dmitriigliuz.linguatwist.console.theming.LogoDecoration
import org.dmitriigliuz.linguatwist.console.theming.Theme
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.*
import org.dmitriigliuz.linguatwist.storage.BackupFailedException
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import java.io.IOException

class ConsoleRenderer(
    private val terminal: Terminal,
    private val theme: Theme,
) {
    private val colorTheme = theme.colorTheme
    private val emoji = theme.emoji

    internal val noResultFeedback = buildErrorFeedback(
        Messages.game_error_unexpected_caption(),
        Messages.game_error_unexpected_message()
    )

    private val roundDisplay = terminal.animation<GameDisplayState> { state -> buildGameWidget(state) }

//    Beginning

    internal fun printLogo() {
        when (theme.logoDecoration) {
            LogoDecoration.PANEL -> terminal.println(
                Panel(
                    Text(theme.logo),
                    padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
                    borderStyle = colorTheme.panelBorder
                )
            )

            LogoDecoration.HORIZONTAL_LINE -> {
                terminal.println(
                    Text(theme.logo)
                )
                terminal.println(HorizontalRule())
            }
        }
        terminal.println()
    }

    internal fun printGreetings() {
        val greeting = buildString {
            appendLine(theme.welcomeMessage())
            if (!theme.showMenuInstructions) {
                appendLine()
                append(emoji.controls.prefixed(Messages.game_greetings_useArrowKeys()))
            }
        }
        terminal.println(Text(greeting))
    }

    internal fun printVocabularyInfo(vocabularyResult: VocabularyLoadResult) {
        terminal.success(
            emoji.vocabularyLoaded.prefixed(
                "${vocabularyResult.vocabulary.size} " +
                        Messages.vocabulary_info_loadedWords()
            )
        )
        if (vocabularyResult.duplicates > 0) {
            terminal.warning(
                emoji.warning.prefixed(Messages.vocabulary_info_duplicates()) +
                        ": ${vocabularyResult.duplicates}"
            )
        }
        if (vocabularyResult.invalidRows > 0) {
            terminal.danger(
                emoji.invalidRows.prefixed(Messages.vocabulary_info_invalidRows()) +
                        ": ${vocabularyResult.invalidRows}"
            )
        }
    }

    internal fun printAllowedMistakesNumber(allowedMistakes: Int) = terminal.println(
        emoji.allowedMistakes.prefixed("${Messages.game_allowedMistakes()}: $allowedMistakes")
    )

    internal fun printStartingGameMessage() {
        val message = theme.startingGameMessage().ifBlank { return }
        emptyLine()
        terminal.println(message)
        emptyLine()
    }

//    Round flow

    internal fun updateGameDisplay(state: GameDisplayState) = roundDisplay.update(state)

    internal fun stopRoundDisplay() = roundDisplay.stop()

    internal fun buildRoundResultFeedback(roundResult: RoundResult): Feedback {
        return when (roundResult) {
            is RoundResult.Correct -> Feedback.Correct(
                emoji.framed(emoji.correct, Messages.round_result_correct(), emoji.plusOnePoint)
                        + "\n${roundResult.words.first} ⟷ ${roundResult.words.second}"
            )

            is RoundResult.Wrong -> Feedback.Wrong(
                emoji.framed(emoji.wrong, Messages.round_result_wrong(), emoji.minusOnePoint) +
                        "\n${roundResult.words.first} ↮ ${roundResult.words.second}"
            )

            is RoundResult.Invalid -> buildInputErrorFeedback(roundResult.error)
        }
    }

    internal fun buildInputErrorFeedback(error: ValidationError? = null, errorMessage: String? = null): Feedback {
        val message = errorMessage ?: when (error) {
            null -> Messages.input_error_default()
            ValidationError.INDEX_OUT_OF_RANGE -> Messages.input_error_indexOutOfRange()
            ValidationError.PAIR_HAS_ALREADY_BEEN_GUESSED -> Messages.input_error_pairAlreadyGuessed()
            ValidationError.PAIR_HAS_ALREADY_BEEN_WRONG -> Messages.input_error_pairAlreadyWrong()
        }
        return buildErrorFeedback(Messages.input_error_caption(), message)
    }

    internal fun buildErrorFeedback(caption: String = Messages.error_caption(), message: String) = Feedback.Invalid(
        emoji.error.prefixed(caption) + "\n" + message
    )

    internal fun buildGameResultFeedback(result: GameResult) = Feedback.Finished(buildString {
        appendLine(
            if (result.isFailed)
                emoji.youFailed.prefixed(Messages.game_result_failed())
            else emoji.youWon.prefixed(Messages.game_result_finished())
        )
        append(emoji.guessedPairs.prefixed(Messages.game_result_guessedPairs()))
        appendLine(": ${result.totalGuessedAmount} / ${result.totalPairsAmount}")
        append(emoji.mistakes.prefixed(Messages.game_result_mistakes()))
        appendLine(": ${result.mistakesCount} / ${result.allowedMistakes}")
        appendLine(buildVerbalScoreOutput(result.verbalScore))
        val duration = result.duration.toComponents { minutes, seconds, _ ->
            val formattedSeconds = seconds.toString().padStart(2, '0')
            "$minutes:$formattedSeconds"
        }
        append(emoji.duration.prefixed(Messages.game_result_duration()))
        append(": $duration")
    })

    private fun buildVerbalScoreOutput(verbalScore: VerbalScore): String {
        val scoreEmoji = when (verbalScore) {
            VerbalScore.PERFECT -> emoji.scorePerfect
            VerbalScore.ALMOST_PERFECT -> emoji.scoreAlmostPerfect
            VerbalScore.EXCELLENT -> emoji.scoreExcellent
            VerbalScore.VERY_GOOD -> emoji.scoreVeryGood
            VerbalScore.GOOD -> emoji.scoreGood
            VerbalScore.ABOVE_AVERAGE -> emoji.scoreAboveAverage
            VerbalScore.AVERAGE -> emoji.scoreAverage
            VerbalScore.BELOW_AVERAGE -> emoji.scoreBelowAverage
            VerbalScore.POOR -> emoji.scorePoor
            VerbalScore.VERY_POOR -> emoji.scoreVeryPoor
            VerbalScore.TRY_AGAIN -> emoji.scoreTryAgain
        }
        return scoreEmoji.prefixed(verbalScore.description.toString())
    }

    private fun buildGameWidget(state: GameDisplayState): Widget = verticalLayout {
        val roundTitle = Text(
            colorTheme.title(
                "${emoji.round.prefixed(Messages.round_caption())} ${state.round.number} / ${state.totalRounds}"
            )
        )
        val (selectedLeft, selectedRight) = state.selectedIndexes
        val roundTable = buildRoundTable(state.round, selectedLeft, selectedRight)
        val gameStats = Text(
            colorTheme.mistake(
                emoji.mistakes.prefixed(Messages.round_stats_mistakes()) +
                        ": ${state.mistakesCount} / ${state.allowedMistakes}"
            )
        )
        val feedbackPanelStyle = when (state.feedback) {
            is Feedback.Correct -> colorTheme.success
            is Feedback.Wrong -> colorTheme.wrong
            is Feedback.Invalid -> colorTheme.error
            else -> colorTheme.feedbackPanelBorder
        }
        val feedbackPanel = Panel(
            content = state.feedback.message,
            borderStyle = feedbackPanelStyle,
            padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
            borderType = theme.feedbackPanelBorderType
        )
        val progressBar = ProgressBar(
            completed = state.completionPercentage.toLong(),
            total = 100,
            completeStyle = colorTheme.progressBarCompleted,
            pendingStyle = colorTheme.progressBarPending,
            showPulse = true
        )
        val prompt = when (state.promptType) {
            RoundDisplayPrompt.ENTER_PAIR -> emoji.inputIndexes.prefixed(Messages.round_input_prompt()) + ": "
            RoundDisplayPrompt.CONFIRM_EXIT -> emoji.exit.prefixed(Messages.prompt_confirmExit() + " (y/N): ")
            else -> ""
        }
        cell(roundTitle)
        cell(roundTable)
        cell(gameStats)
        cell(feedbackPanel)
        cell(progressBar)
        cell(prompt + state.userInput)
    }

    private fun buildRoundTable(round: Round, selectedIndexLeft: Int?, selectedIndexRight: Int?): Widget {
        fun detectValueState(indexInTable: Int, column: Column): RoundValueState {
            val originalIndex = if (column == Column.RIGHT) {
                round.originalPairIndex(indexInTable)
            } else indexInTable
            return if ((selectedIndexLeft == originalIndex && column == Column.LEFT) || (selectedIndexRight == indexInTable && column == Column.RIGHT)) {
                if (originalIndex in round.guessedIndexes) {
                    RoundValueState.GUESSED_SELECTED
                } else RoundValueState.SELECTED
            } else if (originalIndex in round.guessedIndexes) {
                RoundValueState.GUESSED
            } else RoundValueState.DEFAULT
        }

        fun applyStyle(text: String, state: RoundValueState, defaultStyle: TextStyle = colorTheme.defaultText) =
            when (state) {
                RoundValueState.DEFAULT -> defaultStyle(text)
                RoundValueState.SELECTED -> colorTheme.selectedValue(text)
                RoundValueState.GUESSED -> colorTheme.guessedValue(text)
                RoundValueState.GUESSED_SELECTED -> colorTheme.wrongSelectedValue(text)
            }

        fun printedIndex(index: Int, state: RoundValueState): String {
            val printedIndex = (index + 1).toString()
            return applyStyle(printedIndex, state, colorTheme.index)
        }

        val roundTable = table {
//            Without these widths, Mordant in some cases draws the table without the second column with indexes
//            Probably because of some bug in the way Mordant calculates proportions
            column(0) { width = ColumnWidth.Fixed(1) }
            column(2) { width = ColumnWidth.Fixed(1) }
            borderType = BorderType.ROUNDED
            borderStyle = colorTheme.roundTableBorderStyle
            body {
                cellBorders = Borders.NONE
                column(1) { cellBorders = Borders.RIGHT }
                for (indexInTable in 0 until round.size) {
                    val leftPair = round.pairAtLeftColumn(indexInTable)
                    val leftState = detectValueState(indexInTable, Column.LEFT)
                    val leftPrintedIndex = printedIndex(indexInTable, leftState)
                    val leftText = applyStyle(leftPair.sourceText, leftState)

                    val rightPair = round.pairAtRightColumn(indexInTable)
                    val rightState = detectValueState(indexInTable, Column.RIGHT)
                    val rightPrintedIndex = printedIndex(indexInTable, rightState)
                    val rightText = applyStyle(rightPair.translation, rightState)
                    row(leftPrintedIndex, leftText, rightPrintedIndex, rightText)
                }
            }
        }
        return Panel(
            content = roundTable,
            borderStyle = colorTheme.panelBorder,
            borderType = theme.roundPanelBorderType
        )
    }

//    Final

    internal fun buildSaveErrorMessage(error: Throwable): String = when (error) {
        is BackupFailedException -> Messages.file_error_unableToBackup()
        is IOException -> Messages.file_error_failedToSave()
        else -> error.message ?: error.toString()
    }

    internal fun printBye() = renderOrPlain("\n" + emoji.bye.prefixed(Messages.app_shutdown_bye()) + "\n")

//    General

    internal fun <T> withProgressMessage(
        message: String,
        errorMessageBuilder: (Throwable) -> String = { e -> e.message ?: e.toString() },
        action: () -> Result<T>
    ): Result<T> {
        renderOrPlain(emoji.processing.prefixed(message))
        return action()
            .onSuccess {
                renderOrPlain(" " + Messages.game_progress_done(),  terminal::success,  true)
            }.onFailure {
                renderOrPlain(errorMessageBuilder(it), ::printError, true)
            }
    }

    private fun renderOrPlain(
        message: String,
        render: (String) -> Unit = terminal::print,
        newLine: Boolean = false
    ) = runCatching {
        render(message)
    }.onFailure {
        print(message)
        if (newLine) println()
    }

    internal fun emptyLine() = renderOrPlain("\n")

    internal fun printError(message: String) = terminal.danger(emoji.error.prefixed(message))
}

private enum class Column {
    LEFT, RIGHT
}

private enum class RoundValueState {
    DEFAULT, GUESSED, SELECTED, GUESSED_SELECTED
}