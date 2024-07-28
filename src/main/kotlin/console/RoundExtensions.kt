package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Round
import org.dmitriigliuz.linguatwist.core.localeString
import org.dmitriigliuz.linguatwist.core.numberOfPairsInRound

fun Round.outputLines(): List<String> = buildList {
    val maxLength = pairs.maxOf { it.originalWord.length } + 3
    for (numberOfItem in 1..numberOfPairsInRound) {
        val index = numberOfItem - 1
        val leftPair = pairs[index]
        val rightPair = pairs[shuffledIndexes[index]]
        val firstWordOutput = leftPair.run { applyStrikethroughIfGuessed(originalWord) }
        val secondWordOutput = rightPair.run { applyStrikethroughIfGuessed(translation) }
        val spaces = " ".repeat(maxLength - leftPair.originalWord.length)
        add("$numberOfItem. $firstWordOutput$spaces$numberOfItem. $secondWordOutput")
    }
}

fun Round.card(): String {
    val bottomCaption = if (mistakes > 0) "${localeString("MistakesInRound")}: $mistakes" else ""
    val caption = "${localeString("Round")} $number"
    val maxLengthOfString = if (maxStringLengthsInRounds.size >= number) maxStringLengthsInRounds[index] else -1
    return outputLines().drawFrame(caption, bottomCaption, maxLengthOfString)
}