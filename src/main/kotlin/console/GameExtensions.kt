package org.dmitriigliuz.learnnewwords.console

import org.dmitriigliuz.learnnewwords.core.Game

fun Game.printCurrentRound() {
    val progressBarLength = 20
    val progressBar = buildString {
        val percents = completionPercentage()
        val progress = percents * progressBarLength / 100
        append("🌟Score: $score\t❌Mistakes: $mistakes")
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
    val indexes = input("Enter indexes for matching words:")
        .split("""\s+""".toRegex())
        .map { it.toInt() - 1 }
    val round = currentRound()
    try {
        val (result, words) = choosePair(indexes)
        val (firstWord, secondWord) = words
        println("Your choice:\n" +
                "${indexes[0] + 1}. $firstWord\n" +
                "${indexes[1] + 1}. $secondWord\n")
        if (round.isFinished()) println(round.getFormattedOutput())
        println(getFormattedResult(result))
    } catch (e: IllegalArgumentException) {
        println("⚠️ Error: ${e.message}.\n   Try again!\n")
    }
}

fun Game.run() {
    while (!isFinished()) {
        printCurrentRound()
        enterIndexesAndPrintResult()
    }
    printCurrentRound()
    vocabulary.printStatistics()
    println(if (isFailed) {
        "😢 Sorry, you have made three mistakes and failed the game!"
    } else {
        "🎉 Congratulations! You have finished the game!"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 Your result: $percent%\n${score.emoji} ${score.description} ${score.emoji}\n")
}