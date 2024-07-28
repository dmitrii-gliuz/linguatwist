package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.localeString

fun Game.progressBar(progressBarLength: Int): String {
    val percents = completionPercentage().also { when (it) {
        100 -> return "🟩".repeat(progressBarLength) + " 100%"
        0 -> return "⬜".repeat(progressBarLength) + " 0%"
        !in 0..100 -> throw IllegalArgumentException(localeString("PercentageShouldBeInRange"))
    }}
    val progress = percents * progressBarLength / 100
    return buildString {
        append("🟩".repeat(progress)).append("⬜".repeat(progressBarLength - progress)).space().
        append(percents).append("%")
    }
}

/**
 * Adds the maximum length of the strings in the current round to the list of maximum string lengths.
 *
 * This function ensures that the `maxStringLengthsInRounds` list contains the maximum length of the strings
 * in the current round. This is necessary because strikethrough text is longer than regular text, but it is invisible.
 * Therefore, the longest string in the round needs to be considered before applying strikethrough.
 */
fun Game.addMaxLengthOfRound() {
    if (maxStringLengthsInRounds.size < currentRound.number) {
        maxStringLengthsInRounds.add(currentRound.outputLines().maxOf { it.length })
    }
}

fun Game.printCurrentRoundAndGameState() = printbs {
    appendLine(currentRound.card())
    appendLine("🌟${localeString("Score")}: $score  ❌${localeString("Mistakes")}: $mistakes")
    appendLine(progressBar(20))
}

fun Game.choiceResult(indexes: List<Int>, words: List<String>, result: Boolean) = buildString {
    val (first, second) = words
    appendLine("${localeString("YourChoice")}:")
    appendLine("${indexes[0] + 1}. $first")
    appendLine("${indexes[1] + 1}. $second")
    if (currentRound.isFinished()) {
        appendLine(currentRound.card())
    }
    appendLine(formattedResult(result))
}

fun Game.enterIndexesAndPrintResult() {
    var finished = false
    while (!finished) {
        val indexes = try {
            inputIndexes()
        } catch (e: NumberFormatException) {
            printError(e, localeString("InputShouldContainOnlyTwoNumbers"))
            continue
        }
        val (result, words) = try {
            choosePair(indexes)
        } catch (e: IllegalArgumentException) {
            printError(e)
            continue
        }
        println(choiceResult(indexes, words, result))
        finished = true
    }
}

fun Game.run() {
    maxStringLengthsInRounds.clear()
    while (!isFinished()) {
        addMaxLengthOfRound()
        printCurrentRoundAndGameState()
        enterIndexesAndPrintResult()
    }
    val spentTime = spentTime()
    printCurrentRoundAndGameState()
    println(if (isFailed) {
        "😢 ${localeString("SorryYouFailed")}"
    } else {
        "🎉 ${localeString("CongratulationsYouFinished")}"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 ${localeString("YourResult")}: $percent%\n${score.emoji} ${localeString(score.description)} ${score.emoji}")
    println("⏱ ${localeString("CompletionTime")}: $spentTime\n")
}