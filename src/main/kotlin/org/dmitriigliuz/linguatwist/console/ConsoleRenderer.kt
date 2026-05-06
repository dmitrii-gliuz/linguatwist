package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.danger
import com.github.ajalt.mordant.terminal.success
import com.github.ajalt.mordant.terminal.warning
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.ProgressBar
import com.github.ajalt.mordant.widgets.Text
import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.verticalLayout
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.RoundResult
import org.dmitriigliuz.linguatwist.model.ValidationError
import org.dmitriigliuz.linguatwist.model.VerbalScore

internal class ConsoleRenderer(
    private val terminal: Terminal,
    private val colorTheme: ColorTheme,
    private val emoji: EmojiSet
) {
    internal val noResultFeedback = buildErrorFeedback(
        Messages.game_error_unexpected_caption(),
        Messages.game_error_unexpected_message()
    )

    private val roundDisplay = terminal.animation<RoundDisplayState> { state -> buildRoundWidget(state) }

//    Beginning

    internal fun printLogo() {
        val art = colorTheme.logo("""
█   █ █▄ █ █▀▀ █ █ █▀█ ▀█▀ █   █ █ ▄▀▀ ▀█▀
█   █ █ ▀█ █ ▀ █ █ █▀█  █  █ █ █ █  ▀▄  █ 
▀▀▀ ▀ ▀  ▀ ▀▀▀ ▀▀▀ ▀ ▀  ▀   ▀ ▀  ▀ ▀▀▀  ▀""".trimIndent())
        terminal.println(
            Panel(Text(art),
                padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
                borderStyle = colorTheme.panelBorder
            )
        )
    }

    internal fun printGreetings() {
        terminal.println(Text("""
            ${emoji.welcome.prefixed(Messages.game_greetings_welcome())} ${Messages.game_greetings_matchWords}
            ${emoji.advice.prefixed(Messages.game_greetings_enterTwoNumbers())}
            ${emoji.controls.prefixed(Messages.game_greetings_useArrowKeys())}""".trimIndent())
        )
    }

    internal fun printVocabularyInfo(vocabularyResult: VocabularyLoadResult) {
        terminal.success(emoji.vocabularyLoaded.prefixed(Messages.vocabulary_info_loadedWords()) +
                ": ${vocabularyResult.vocabulary.size}")
        if (vocabularyResult.duplicates > 0) {
            terminal.warning(emoji.warning.prefixed(Messages.vocabulary_info_duplicates()) +
                    ": ${vocabularyResult.duplicates}")
        }
        if (vocabularyResult.invalidRows > 0) {
            terminal.danger(emoji.invalidRows.prefixed(Messages.vocabulary_info_invalidRows()) +
                    ": ${vocabularyResult.invalidRows}")
        }
    }

    internal fun printAllowedMistakesNumber(allowedMistakes: Int) = terminal.println(
        emoji.allowedMistakes.prefixed("${Messages.game_allowedMistakes}: $allowedMistakes")
    )

    internal fun printLetsGo() = terminal.println(
        colorTheme.coloredMessage (emoji.letsGo.prefixed(Messages.game_letsGo()))
    )

//    Round flow

    internal fun updateRoundDisplay(state: RoundDisplayState) = roundDisplay.update(state)

    internal fun stopRoundDisplay() = roundDisplay.stop()

    internal fun buildRoundResultFeedback(roundResult: RoundResult) : String {
        return when (roundResult) {
            is RoundResult.Correct -> colorTheme.success(
                emoji.framed(emoji.correct, Messages.round_result_correct(), emoji.plusOnePoint)
                        + "\n${roundResult.words.first} ⟷ ${roundResult.words.second}")
            is RoundResult.Wrong -> colorTheme.warning(
                emoji.framed(emoji.wrong, Messages.round_result_wrong(), emoji.minusOnePoint) +
                        "\n${roundResult.words.first} ↮ ${roundResult.words.second}")
            is RoundResult.Invalid -> buildInputErrorFeedback(roundResult.error)
        }
    }

    internal fun buildInputErrorFeedback(error: ValidationError? = null, errorMessage: String? = null): String {
        val message = errorMessage ?: when (error) {
            null -> Messages.input_error_default()
            ValidationError.INDEX_OUT_OF_RANGE -> Messages.input_error_indexOutOfRange()
            ValidationError.PAIR_HAS_ALREADY_BEEN_GUESSED -> Messages.input_error_pairAlreadyGuessed()
            ValidationError.PAIR_HAS_ALREADY_BEEN_WRONG -> Messages.input_error_pairAlreadyWrong()
        }
        return buildErrorFeedback(Messages.input_error_caption(), message)
    }

    internal fun buildErrorFeedback(caption: String = Messages.error_caption(), message: String) = buildString {
        appendLine(emoji.error.prefixed(colorTheme.error(caption)))
        append(message)
    }

    internal fun buildGameResultOutput(result: GameResult) = buildString {
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
    }

    private fun buildVerbalScoreOutput(verbalScore: VerbalScore): String {
        val scoreEmoji =  when (verbalScore) {
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

    private fun buildRoundWidget(state: RoundDisplayState): Widget = verticalLayout {
        val roundTable = buildRoundTable(state.round)
        val gameStats = Text(
            colorTheme.scoreStyle(emoji.score.prefixed(Messages.round_stats_score()) + ": ${state.score}   ") +
                    colorTheme.mistakeStyle(emoji.mistakes.prefixed(Messages.round_stats_mistakes()) +
                            ": ${state.totalMistakesMade}")
        )

        val feedback = Panel(
            content = state.feedbackMessage,
            borderStyle = colorTheme.feedbackPanelBorder,
            padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
            borderType = BorderType.ASCII
        )
        val progressBar = ProgressBar(
            completed = state.completionPercentage.toLong(),
            total = 100,
            completeStyle = colorTheme.progressBarCompleted,
            pendingStyle = colorTheme.progressBarPending
        )
        val prompt = when (state.promptType) {
            RoundDisplayPrompt.ENTER_INDEXES -> emoji.inputIndexes.prefixed(Messages.round_input_prompt()) + ": "
            RoundDisplayPrompt.CONFIRM_EXIT -> emoji.exit.prefixed(Messages.game_confirmExit() + " (y/N): ")
            else -> ""
        }
        cell(roundTable)
        cell(gameStats)
        cell(feedback)
        cell(progressBar)
        cell(prompt)
    }

    private fun buildRoundTable(round: Round): Widget {
        fun styleIfGuessed(text: String, index: Int) =
            if (index in round.guessedIndexes) colorTheme.guessedWord(text) else text

        val roundTable = table {
//            Without these widths, Mordant in some cases draws the table without the second column with indexes
//            Probably because of some bug in the way Mordant calculates proportions
            column(0) { width = ColumnWidth.Fixed(1) }
            column(2) { width = ColumnWidth.Fixed(1) }
            borderType = BorderType.BLANK
            body {
                cellBorders = Borders.NONE
                for (i in 0 until round.size) {
                    val leftPair = round.pairAtLeftColumn(i)
                    val rightPair = round.pairAtRightColumn(i)
                    val leftText = styleIfGuessed(leftPair.sourceText, i)
                    val rightText = styleIfGuessed(rightPair.translation, round.originalPairIndex(i))
                    val printedIndex = i + 1
                    row(printedIndex, leftText, printedIndex, rightText)
                }
            }
        }

        val mistakesInRound = if (round.mistakesInRound > 0) {
            colorTheme.mistakeStyle("${Messages.round_mistakesInRound}: ${round.mistakesInRound}")
        } else ""
        return Panel(
            content = roundTable,
            title = Text(colorTheme.title("${Messages.round_caption} ${round.number}")),
            bottomTitle = Text(mistakesInRound),
            borderStyle = colorTheme.panelBorder
        )
    }

//    Final

    internal fun printBye() = terminal.println("\n" + emoji.bye.prefixed(Messages.app_shutdown_bye()))

//    General

    internal fun <T> withProgressMessage(message: String, action: () -> Result<T>): Result<T> {
        terminal.print(Text(emoji.processing.prefixed(message)))
        val result = action()
        result
            .onSuccess { terminal.success(" " + Messages.game_progress_done()) }
            .onFailure { e -> printError(e.message ?: e.toString()) }
        return result
    }

    internal fun emptyLine() = terminal.println()

    internal fun printError(message: String) = terminal.danger(emoji.error.prefixed(message))
}