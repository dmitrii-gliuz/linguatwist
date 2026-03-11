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
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.verticalLayout
import org.dmitriigliuz.linguatwist.model.RoundResult
import org.dmitriigliuz.linguatwist.model.ValidationError


class ConsoleRenderer(
    private val terminal: Terminal,
    private val messages: Messages
) {
    internal val initialFeedback = "🎮 Ready to play? Match words with their translations! 🎯\n" +
            "\uD83D\uDD22 Type two numbers separated by a space (e.g. 1 3)"

    internal fun emptyLine() = terminal.println()

    private val roundDisplay = terminal.animation<RoundDisplayState> { state -> buildRoundWidget(state) }

    internal fun updateRoundDisplay(state: RoundDisplayState) = roundDisplay.update(state)

    internal fun stopRoundDisplay() = roundDisplay.stop()
    
    internal fun printLogo() {
        val art = """
█   █ █▄ █ █▀▀ █ █ █▀█ ▀█▀ █   █ █ ▄▀▀ ▀█▀
█   █ █ ▀█ █ ▀ █ █ █▀█  █  █ █ █ █  ▀▄  █ 
▀▀▀ ▀ ▀  ▀ ▀▀▀ ▀▀▀ ▀ ▀  ▀   ▀ ▀  ▀ ▀▀▀  ▀""".trim()
        terminal.println(
            Panel(Text(art),
                padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
                borderStyle = Theme.neonIndigo
            )
        )
    }

    internal fun buildRoundResultFeedback(roundResult: RoundResult) : String {
        return when (roundResult) {
            is RoundResult.Correct -> Theme.success("✅ Right! +1 point 🔼\n") +
                    "${roundResult.words.first} ⟷ ${roundResult.words.second}"
            is RoundResult.Wrong -> Theme.warning("❌ Wrong! -1 point 🔽\n") +
                    "${roundResult.words.first} ↮ ${roundResult.words.second}"
            is RoundResult.Invalid -> buildErrorFeedback(roundResult.error)
        }
    }

    internal fun buildErrorFeedback(error: ValidationError? = null, errorMessage: String? = null): String {
        val message = errorMessage ?: if (error != null) when (error) {
            ValidationError.INDEX_OUT_OF_RANGE -> "Indexes are out of range"
            ValidationError.PAIR_HAS_ALREADY_BEEN_GUESSED -> "Pair has already been guessed"
        }
        else "The input must contain two numbers separated by space"
        return Theme.error("🚫 Input error\n") + message
    }

    internal fun <T> withProgressMessage(message: String, action: () -> T): T? {
        terminal.print(Text("⏳ $message "))
        return try {
            val result = action()
            terminal.success("Done!")
            result
        } catch (e: Exception) {
            printError(e.toString())
            null
        }
    }

    internal fun printMaxMistakesNumber(maxMistakes: Int) = terminal.println(
        "🚨 Maximal number of possible mistakes: $maxMistakes"
    )

    internal fun printGreetings() {
        terminal.println(Text("""
            👋 Welcome to LinguaTwist! Match words with their translations.
            💡 Enter two numbers separated by a space to pair them. Type 'exit' to quit.""".trimIndent()
        )
        )
    }

    internal fun printLetsGo() = terminal.println(Theme.neonCyan ("🚀 Let's go!"))

    internal fun buildGameResult(result: GameResult) = buildString {
        appendLine(
            if (result.isFailed)
                "😢 Sorry, you failed."
            else "🎉 Congratulations, you finished!"
        )
        appendLine("✅ Guessed pairs: ${result.totalGuessedAmount} / ${result.totalPairsAmount}")
        appendLine("❌ Mistakes: ${result.mistakesCount} / ${result.maxMistakes}")
        append("${result.verbalScore.emoji} ${result.verbalScore.description}")
    }

    internal fun printWarning(message: String) = terminal.warning(message)

    internal fun printError(message: String) = terminal.danger("❌ $message")

    internal fun printVocabularyInfo(vocabularySize: Int, vocabularyResult: VocabularyLoadResult) {
        terminal.success("📚 The number of loaded words: $vocabularySize")
        if (vocabularyResult.duplicates > 0) terminal.warning("⚠️ ${vocabularyResult.duplicates} duplicates found")
        if (vocabularyResult.overlappedPairs > 0) terminal.warning("⚠️ ${vocabularyResult.overlappedPairs} overlapping pairs found")
        if (vocabularyResult.invalidRows > 0) terminal.danger("❌ ${vocabularyResult.invalidRows} invalid rows found")
    }

    private fun buildRoundWidget(state: RoundDisplayState): Widget = verticalLayout {
        val roundTable = buildRoundTable(state.round)
        val gameStats = Text(
            Theme.scoreStyle("🌟 Score: ${state.score}   ") + Theme.mistakeStyle("❌ Mistakes: ${state.totalMistakesMade}")
        )

        val feedback = Panel(
            content = state.feedbackMessage,
            borderStyle = Theme.dimGray,
            padding = Padding(top = 0, right = 1, bottom = 0, left = 1),
            borderType = BorderType.ASCII
        )
        val progressBar = ProgressBar(
            completed = state.completionPercentage.toLong(),
            total = 100,
            completeStyle = Theme.neonGreen,
            pendingStyle = Theme.darkPurple
        )
        val inputIndexesPrompt = "✍️ Input indexes for matching words: "
        cell(roundTable)
        cell(gameStats)
        cell(feedback)
        cell(progressBar)
        cell(inputIndexesPrompt)
    }

    private fun buildRoundTable(round: Round): Widget {
        fun styleIfGuessed(text: String, index: Int) =
            if (index in round.guessedIndexes) Theme.guessedWord(text) else text

        val roundTable = table {
            column(1) { padding = Padding { right = 1 } }
            column(2) { padding = Padding { left = 1 } }
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
            Theme.mistakeStyle("Mistakes in round: ${round.mistakesInRound}")
        } else ""
        return Panel(
            content = roundTable,
            title = Text(Theme.title("Round ${round.number}")),
            bottomTitle = Text(mistakesInRound),
            borderStyle = Theme.neonIndigo
        )
    }
}