package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.Round
import org.dmitriigliuz.linguatwist.core.localeString

fun Round.outputLines(): List<String> = buildList {
    val maxLength = pairs.maxOf { it.originalWord.length } + 3
        for (n in 1..this@outputLines.size) {
            val index = n - 1
            val leftPair = pairs[index]
            val rightPair = pairs[shuffledIndexes[index]]
            val firstWordOutput = leftPair.originalWordStrikethroughIfGuessed()
            val secondWordOutput = rightPair.translationStrikethroughIfGuessed()
            val numberOfSpaces = maxLength - leftPair.originalWordLength()
            val spaces = " ".repeat(numberOfSpaces)
            add("$n. $firstWordOutput$spaces$n. $secondWordOutput")
        }
}

fun Round.card(): String {
    val bottomCaption = if (this.mistakes > 0) "${localeString("MistakesInRound")}: ${this.mistakes}" else ""
    val caption = "${localeString("Round")} ${this.number}"
    val roundOutputLines: List<String> = this.outputLines()
    val maxLengthOfString = if (maxStringLengthsInRounds.size >= this.number) {
        maxStringLengthsInRounds[this.number - 1]
    } else -1
    return roundOutputLines.drawFrame(caption, bottomCaption, maxLengthOfString)
}