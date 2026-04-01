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


internal class ConsoleRenderer(private val terminal: Terminal, private val colorTheme: ColorTheme) {
    internal val noResultFeedback = buildErrorFeedback(
        Messages.game_error_unexpected_caption(),
        Messages.game_error_unexpected_message()
    )

    private val roundDisplay = terminal.animation<RoundDisplayState> { state -> buildRoundWidget(state) }

    internal fun emptyLine() = terminal.println()

    internal fun updateRoundDisplay(state: RoundDisplayState) = roundDisplay.update(state)

    internal fun stopRoundDisplay() = roundDisplay.stop()
    
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

    internal fun buildRoundResultFeedback(roundResult: RoundResult) : String {
        return when (roundResult) {
            is RoundResult.Correct -> colorTheme.success("✅ ${Messages.round_result_correct} 🔼\n") +
                    "${roundResult.words.first} ⟷ ${roundResult.words.second}"
            is RoundResult.Wrong -> colorTheme.warning("❌ ${Messages.round_result_wrong} 🔽\n") +
                    "${roundResult.words.first} ↮ ${roundResult.words.second}"
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
        append("🚫 ")
        appendLine(colorTheme.error(caption))
        append(message)
    }

    internal fun <T> withProgressMessage(message: String, action: () -> Result<T>): T? {
        terminal.print(Text("⏳ $message "))
        return action()
            .onSuccess { terminal.success(Messages.game_progress_done()) }
            .onFailure { e -> printError(e.message ?: e.toString()) }
            .getOrNull()
    }

    internal fun printMaxMistakesNumber(maxMistakes: Int) = terminal.println(
        "🚨 ${Messages.game_maxMistakes}: $maxMistakes"
    )

    internal fun printGreetings() {
        terminal.println(Text("""
            👋 ${Messages.game_greetings_welcome} ${Messages.game_greetings_matchWords}
            💡 ${Messages.game_greetings_enterTwoNumbers}
            🕹️ ${Messages.game_greetings_useArrowKeys}""".trimIndent())
        )
    }

    internal fun printLetsGo() = terminal.println(
        colorTheme.coloredMessage ("🚀 ${Messages.game_letsGo}")
    )

    internal fun buildGameResultOutput(result: GameResult) = buildString {
        appendLine(
            if (result.isFailed)
                "😢 ${Messages.game_result_failed}"
            else "🎉 ${Messages.game_result_finished}"
        )
        appendLine("✅ ${Messages.game_result_guessedPairs}: ${result.totalGuessedAmount} / ${result.totalPairsAmount}")
        appendLine("❌ ${Messages.game_result_mistakes}: ${result.mistakesCount} / ${result.maxMistakes}")
        appendLine("${result.verbalScore.emoji} ${result.verbalScore.description}")
        val duration = result.duration.toComponents { minutes, seconds, _ ->
            val formattedSeconds = seconds.toString().padStart(2, '0')
            "$minutes:$formattedSeconds"
        }
        append("⏳ ${Messages.game_result_duration}: $duration")
    }

    internal fun printError(message: String) = terminal.danger("❌ $message")

    internal fun printVocabularyInfo(vocabularyResult: VocabularyLoadResult) {
        terminal.success("📚 ${Messages.vocabulary_info_loadedWords}: ${vocabularyResult.vocabulary.size}")
        if (vocabularyResult.duplicates > 0) {
            terminal.warning("⚠️ ${Messages.vocabulary_info_duplicates}: ${vocabularyResult.duplicates}")
        }
        if (vocabularyResult.overlappedPairs > 0) {
            terminal.warning("⚠️ ${Messages.vocabulary_info_overlappedPairs}: ${vocabularyResult.overlappedPairs}")
        }
        if (vocabularyResult.invalidRows > 0) {
            terminal.danger("❌ ${Messages.vocabulary_info_invalidRows}: ${vocabularyResult.invalidRows}")
        }
    }

    private fun buildRoundWidget(state: RoundDisplayState): Widget = verticalLayout {
        val roundTable = buildRoundTable(state.round)
        val gameStats = Text(
            colorTheme.scoreStyle("🌟 ${Messages.round_stats_score}: ${state.score}   ") +
                    colorTheme.mistakeStyle("❌ ${Messages.round_stats_mistakes}: ${state.totalMistakesMade}")
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
        val inputIndexesPrompt = "✍️ ${Messages.round_input_prompt}: "
        cell(roundTable)
        cell(gameStats)
        cell(feedback)
        cell(progressBar)
        cell(inputIndexesPrompt)
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
}