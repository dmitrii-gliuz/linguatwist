package org.dmitriigliuz.linguatwist.console

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


class ConsoleRenderer(
    private val terminal: Terminal,
    private val messages: Messages
) {
    fun printLogo() {
        val art = """
    ▖ ▘        ▄▖   ▘  ▗
    ▌ ▌▛▌▛▌▌▌▀▌▐ ▌▌▌▌▛▘▜
    ▙▖▌▌▌▙▌▙▌█▌▐ ▚▚▘▌▄▌▐▖
         ▄▌         ── v0.2 ──
⟐  L E A R N  · S U R V I V E  ⟐""".trimMargin()
        val colors = listOf(Theme.neonCyan, Theme.neonMagenta, Theme.neonGreen)
        val coloredArt = buildString {
            art.lines().forEachIndexed { i, line ->
                if (i < art.lines().size - 1) appendLine(colors[i % colors.size](line))
                else append(colors[i % colors.size](line))
            }
        }
        terminal.println(Panel(Text(coloredArt), padding = Padding(top = 0, right = 1, bottom = 0, left = 1)))
    }
    

    fun printRoundCard(round: Round) {
        fun styleIfGuessed(text: String, index: Int) = if (index in round.guessedIndexes) Theme.guessedWord(text) else text
        terminal.println(
            table {
                captionTop(Theme.roundTitle("⟐ Round ${round.number} ⟐"))
                header { row(
                    Theme.accent("N"),
                    Theme.accent("Original"),
                    Theme.accent("#"),
                    Theme.accent("Translation"
                    ))
                }
                body {
                    for (i in round.pairs.indices) {
                        val leftPair = round.pairs[i]
                        val rightPair = round.pairs[round.rightColumnPermutation[i]]
                        val leftText = styleIfGuessed(leftPair.sourceText, i)
                        val rightText = styleIfGuessed(rightPair.translation, round.rightColumnPermutation[i])
                        val printedIndex = i + 1
                        row(printedIndex, leftText, rightText)
                    }
                }
                captionBottom(
                    if (round.mistakesCount > 0) Theme.mistakeStyle("Mistakes: ${round.mistakesCount}")
                    else ""
                )
            }
        )
    }

    fun printGameStatus(score: Int, mistakes: Int, completionPercentage: Int) {
        terminal.println(
            Theme.scoreStyle("★ Score: $score") + "   " +
            Theme.mistakeStyle("Mistakes: $mistakes")
        )
        terminal.println(ProgressBar(
            completed = completionPercentage.toLong(),
            total = 100,
            completeStyle = Theme.neonGreen,
            pendingStyle = Theme.dimGray
        ))
    }

    fun printCorrect() {
        terminal.success("✓ Right!")
        terminal.println("  +1 point")
    }

    fun printWrong() {
        terminal.danger("✗ Wrong!")
        terminal.println("  -1 point")
    }

    fun printGameResult(result: GameResult) {
        val panel = Panel(
            title = Text(Theme.title("⟐ Result ⟐")),
            content = Text(buildString {
                appendLine(
                    if (result.isFailed)
                        "😢 Sorry, you failed."
                    else "🎉 Congratulations, you finished!"
                )
                appendLine("Your result: ${result.percentage}%")
                appendLine(result.verbalScore.description)
            })
        )
    }
    fun printWarning(message: String) = terminal.warning(message)
    fun printError(message: String) = terminal.danger(message)

    fun printVocabularyInfo(vocabularySize: Int, vocabularyResult: VocabularyLoadResult) {
        terminal.success("The number of loaded words: $vocabularySize")
        if (vocabularyResult.duplicates > 0) terminal.warning("${vocabularyResult.duplicates} duplicates found")
        if (vocabularyResult.overlappedPairs > 0) terminal.warning("${vocabularyResult.overlappedPairs} overlapping pairs found")
        if (vocabularyResult.invalidRows > 0) terminal.danger("${vocabularyResult.invalidRows} invalid rows found")
    }
}