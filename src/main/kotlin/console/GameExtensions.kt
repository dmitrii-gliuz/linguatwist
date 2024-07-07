package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.getLocaleMessage

fun Game.printCurrentRound() {
    val progressBarLength = 20
    val progressBar = buildString {
        val percents = completionPercentage()
        val progress = percents * progressBarLength / 100
        append("🌟${getLocaleMessage("Score")}: $score\t❌${getLocaleMessage("Mistakes")}: $mistakes")
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
    val indexes = input("${getLocaleMessage("EnterIndexesForMatchingWords")}:")
        .trim()
        .split("""\s+""".toRegex())
        .map { it.toInt() - 1 }
    val round = currentRound()
    try {
        val (result, words) = choosePair(indexes)
        val (firstWord, secondWord) = words
        println("${getLocaleMessage("YourChoice")}:\n" +
                "${indexes[0] + 1}. $firstWord\n" +
                "${indexes[1] + 1}. $secondWord\n")
        if (round.isFinished()) println(round.getFormattedOutput())
        println(getFormattedResult(result))
    } catch (e: IllegalArgumentException) {
        println("⚠️ ${getLocaleMessage("Error")}: ${e.message}.\n   ${getLocaleMessage("TryAgain")}!\n")
    }
}

fun Game.run() {
    while (!isFinished()) {
        printCurrentRound()
        enterIndexesAndPrintResult()
    }
    printCurrentRound()
    println(if (isFailed) {
        "😢 ${getLocaleMessage("SorryYouFailed")}"
    } else {
        "🎉 ${getLocaleMessage("CongratulationsYouFinished")}"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 ${getLocaleMessage("YourResult")}: $percent%\n${score.emoji} ${getLocaleMessage(score.description)} ${score.emoji}\n")
}