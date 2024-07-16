package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.getLocaleMessage

fun Game.printCurrentRound() {
    if (maxStringLengthsInRounds.size < currentRound().number) {
        maxStringLengthsInRounds.add(currentRound().outputLines().maxOf { it.length })
    }
    val progressBarLength = 20
    val result = "🌟${getLocaleMessage("Score")}: $score  ❌${getLocaleMessage("Mistakes")}: $mistakes"
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
    println(currentRound().getFormattedOutput())
    println(progressBar)
}

fun Game.enterIndexesAndPrintResult() {
    var errorHappened: Boolean
    do {
        try {
            val indexes = input("${getLocaleMessage("EnterIndexesForMatchingWords")}:")
                .trim()
                .split("""\s+""".toRegex())
                .map { it.toInt() - 1 }
            val round = currentRound()
            val (result, words) = choosePair(indexes)
            val (firstWord, secondWord) = words
            println("${getLocaleMessage("YourChoice")}:\n" +
                    "${indexes[0] + 1}. $firstWord\n" +
                    "${indexes[1] + 1}. $secondWord\n")
            if (round.isFinished()) println(round.getFormattedOutput())
            println(getFormattedResult(result))
            errorHappened = false
        } catch (e: NumberFormatException) {
            println("⚠️ ${getLocaleMessage("Error")}: The input should contain numbers.\n   ${getLocaleMessage("TryAgain")}!\n")
            errorHappened = true
        } catch (e: IllegalArgumentException) {
            println("⚠️ ${getLocaleMessage("Error")}: ${e.message}\n   ${getLocaleMessage("TryAgain")}!\n")
            errorHappened = true
        } catch (e: IndexOutOfBoundsException) {
            println("⚠️ ${getLocaleMessage("Error")}: ${getLocaleMessage("IndexesShouldBeInTheRange")}\n   ${getLocaleMessage("TryAgain")}!\n")
            errorHappened = true
        }
    } while (errorHappened)
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
    println("🎯 ${getLocaleMessage("YourResult")}: $percent%\n${score.emoji} ${getLocaleMessage(score.description)} ${score.emoji}")
    val spentTime = getSpentTime()
    println("⏱ ${getLocaleMessage("CompletionTime")}: $spentTime\n")
}