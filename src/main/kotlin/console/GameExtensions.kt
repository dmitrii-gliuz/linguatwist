package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.localeString

fun Game.progressBar(progressBarLength: Int): String {
    val percents = completionPercentage()
    if (percents !in 0..100) throw IllegalArgumentException("The percentage should be in the range from 0 to 100.")
    val progress = percents * progressBarLength / 100
    return buildString {
        append("🟩".repeat(progress))
        append("⬜".repeat(progressBarLength - progress))
        append(" ")
        append(percents)
        append("%")
    }
}

fun Game.printCurrentRoundAndGameState() {
    if (maxStringLengthsInRounds.size < currentRound.number) {
        maxStringLengthsInRounds.add(currentRound.outputLines().maxOf { it.length })
    }
    println(currentRound.card())
    println("🌟${localeString("Score")}: $score  ❌${localeString("Mistakes")}: $mistakes")
    println(progressBar(20))
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
        val (first, second) = words
        println(buildString {
            append("${localeString("YourChoice")}:\n")
            append("${indexes[0] + 1}. $first\n")
            append("${indexes[1] + 1}. $second\n")
            if (currentRound.isFinished()) {
                append(currentRound.card())
                newLine()
            }
            append(getFormattedResult(result))
        })
        finished = true
    }
}

fun Game.run() {
    maxStringLengthsInRounds.clear()
    while (!isFinished()) {
        printCurrentRoundAndGameState()
        enterIndexesAndPrintResult()
    }
    printCurrentRoundAndGameState()
    println(if (isFailed) {
        "😢 ${localeString("SorryYouFailed")}"
    } else {
        "🎉 ${localeString("CongratulationsYouFinished")}"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 ${localeString("YourResult")}: $percent%\n${score.emoji} ${localeString(score.description)} ${score.emoji}")
    val spentTime = getSpentTime()
    println("⏱ ${localeString("CompletionTime")}: $spentTime\n")
}