package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.message

fun Game.printCurrentRound() {
    val progressBarLength = 20
    val progressBar = buildString {
        val percents = completionPercentage()
        val progress = percents * progressBarLength / 100
        append("🌟${message("Score")}: $score\t❌${message("Mistakes")}: $mistakes")
        newLine()
        append("🟩".repeat(progress))
        append("⬜".repeat(progressBarLength - progress))
        append(" ")
        append(percents)
        append("%")
        newLine()
    }
    println(currentRound().getFormattedOutput())
    println(progressBar)
}

fun Game.enterIndexesAndPrintResult() {
    val indexes = input("${message("EnterIndexesForMatchingWords")}:")
        .trim()
        .split("""\s+""".toRegex())
        .map { it.toInt() - 1 }
    val round = currentRound()
    try {
        val (result, words) = choosePair(indexes)
        val (firstWord, secondWord) = words
        println("${message("YourChoice")}:\n" +
                "${indexes[0] + 1}. $firstWord\n" +
                "${indexes[1] + 1}. $secondWord\n")
        if (round.isFinished()) println(round.getFormattedOutput())
        println(getFormattedResult(result))
    } catch (e: IllegalArgumentException) {
        println("⚠️ ${message("Error")}: ${e.message}.\n   ${message("TryAgain")}!\n")
    }
}

fun Game.run() {
    while (!isFinished()) {
        printCurrentRound()
        enterIndexesAndPrintResult()
    }
    printCurrentRound()
    println(if (isFailed) {
        "😢 ${message("SorryYouFailed")}"
    } else {
        "🎉 ${message("CongratulationsYouFinished")}"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 ${message("YourResult")}: $percent%\n${score.emoji} ${message(score.description)} ${score.emoji}\n")
}