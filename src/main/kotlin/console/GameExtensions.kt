package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Game
import org.dmitriigliuz.linguatwist.core.localeString

fun Game.progressBar(progressBarLength: Int): String {
    val square = if (useEmojis) Emojis.ProgressBar.toString() else "#"
    val emptySquare = if (useEmojis) Emojis.ProgressBarEmpty.toString() else " "
    val bar: String
    val percents = completionPercentage()
    bar = when (percents) {
        100 -> square.repeat(progressBarLength)
        0 -> emptySquare.repeat(progressBarLength)
        !in 0..100 -> throw IllegalArgumentException(localeString("PercentageShouldBeInRange"))
        else -> {
            val progress = percents * progressBarLength / 100
            buildString { append(square.repeat(progress))
                append(emptySquare.repeat(progressBarLength - progress))
            }
        }
    }
    return buildString {
        if (!useEmojis) append("[")
        append(bar)
        if (!useEmojis) append("]")
        space()
        append("$percents%")
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
    appendLine("${Emojis.ScoresSaved.withoutSpace()}${localeString("Score")}: $score  ${Emojis.Mistakes.withoutSpace()}${localeString("Mistakes")}: $mistakes")
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
        "${Emojis.Sorry}${localeString("SorryYouFailed")}"
    } else {
        "${Emojis.Congratulations}${localeString("CongratulationsYouFinished")}"
    })
    val (percent, score) = percentageAndVerbalScore()
    val scoreOutput = if (useEmojis) {
        "${score.emoji} ${localeString(score.description)} ${score.emoji}"
    } else {
        localeString(score.description)
    }

    println("${Emojis.YourResult}${localeString("YourResult")}: $percent%\n$scoreOutput")
    println("${Emojis.CompletionTime}${localeString("CompletionTime")}: $spentTime\n")
}