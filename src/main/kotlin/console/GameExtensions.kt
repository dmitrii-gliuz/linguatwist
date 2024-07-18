package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.localeString

fun Game.printCurrentRoundAndGameState() {
    if (maxStringLengthsInRounds.size < currentRound.number) {
        maxStringLengthsInRounds.add(currentRound.outputLines().maxOf { it.length })
    }
    val progressBarLength = 20
    val result = "🌟${localeString("Score")}: $score  ❌${localeString("Mistakes")}: $mistakes"
    val progressBar = buildString {
        val percents = completionPercentage()
        val progress = percents * progressBarLength / 100
        append(result)
        newLine()
        append("🟩".repeat(progress))
        append("⬜".repeat(progressBarLength - progress))
        append(" ")
        append(percents)
        append("%")
        newLine()
    }
    println(currentRound.card())
    println(progressBar)
}

fun Game.enterIndexesAndPrintResult() {
    var errorHappened: Boolean
    do {
        try {
            val indexes = input("${localeString("EnterIndexesForMatchingWords")}:")
                .trim()
                .split("""\s+""".toRegex())
                .map { it.toInt() - 1 }
//            val round = currentRound
            val (result, words) = choosePair(indexes)
            val (firstWord, secondWord) = words
            println("${localeString("YourChoice")}:\n" +
                    "${indexes[0] + 1}. $firstWord\n" +
                    "${indexes[1] + 1}. $secondWord\n")
            if (currentRound.isFinished()) println(currentRound.card())
            println(getFormattedResult(result))
            errorHappened = false
        } catch (e: NumberFormatException) {
            println("⚠️ ${localeString("Error")}: The input should contain numbers.\n   ${localeString("TryAgain")}!\n")
            errorHappened = true
        } catch (e: IllegalArgumentException) {
            println("⚠️ ${localeString("Error")}: ${e.message}\n   ${localeString("TryAgain")}!\n")
            errorHappened = true
        } catch (e: IndexOutOfBoundsException) {
            println(
                "⚠️ ${localeString("Error")}: ${localeString("IndexesShouldBeInTheRange")}\n   ${localeString("TryAgain")}!\n")
            errorHappened = true
        }
    } while (errorHappened)
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