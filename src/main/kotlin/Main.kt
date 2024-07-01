package org.dmitriigliuz.learnnewwords

import kotlin.system.exitProcess


const val bye = "👋 Bye!"
const val exitCommand = "exit"

fun input(message: String): String {
    print("👉 $message ")
    val userInput = readln().trim()
    if (userInput == exitCommand) {
        println(bye)
        exitProcess(0)
    }
    return userInput
}

fun inputPairIndexes(): List<Int> {
    val userInput = input("Enter indexes for matching words:")
    val indexes = userInput.split("""\s+""".toRegex())
    return indexes.map { it.toInt() - 1 }
}

fun printProgressBar(percents: Int) {
    val progressBarLength = 20
    val progressBar = buildString {
        val progress = percents * progressBarLength / 100
        append("🟩".repeat(progress))
        append("⬜".repeat(progressBarLength - progress))
        append(" ")
        append(percents)
        append("%")
        newLine()
    }
    println(progressBar)
}

private fun Game.enterIndexesAndPrintResult() {
    val indexes = inputPairIndexes()
    val roundNumberBefore = currentRoundNumber

    try {
        val (result, words) = choosePair(indexes)
        val (firstWord, secondWord) = words
        println("Your choice:\n" +
                "${indexes[0] + 1}. $firstWord\n" +
                "${indexes[1] + 1}. $secondWord\n")
        if (roundNumberBefore != currentRoundNumber) {
            println(outputRound(this.previousRound()))
        }
        val printingResult = if (result) "✅ Right!\n🔼 +1" else "❌ Wrong!\n🔽 -1"
        println("$printingResult point\n")
    } catch (e: IllegalArgumentException) {
        println("⚠️ Error: ${e.message}.\n   Try again!\n")
    }

}
val horizontalLine = buildString {
    append("=".repeat(35))
    newLine()
}

fun outputRound(round: Round) = buildString {
    val roundProgressEmoji = when (round.guessedPairs) {
            0 -> "🔵" // Beginning of the round
            1 -> "🟢" // Early stage of the round
            2 -> "🟡" // Middle stage of the round
            3 -> "🟠" // Late stage of the round
            else -> "🔴" // End of the round
        }
    append("$roundProgressEmoji Round ${round.number}")
    if (round.mistakes > 0) append(" (Mistakes: ${round.mistakes})")
    newLine()
    append(horizontalLine)
    append(round)
    append(horizontalLine)
}

fun printRoundScore(game: Game) {
    print(outputRound(game.currentRound()))
    println("🌟Score: ${game.score}\t❌Mistakes: ${game.mistakes}\n")
    printProgressBar(game.completionPercentage())
}

fun gameProcess(game: Game) {
    while (!game.isFinished()) {
        printRoundScore(game)
        game.enterIndexesAndPrintResult()
    }
    printRoundScore(game)
    println(if (game.isFailed) {
        "😢 Sorry, you have made three mistakes and failed the game!"
    } else {
        "🎉 Congratulations! You have finished the game!"
    })
    val (percent, score) = game.getPercentageAndVerbalScore()
    println("🎯 Your result: $percent%\n${score.emoji} ${score.description} ${score.emoji}\n")
}


fun playAgain(): Boolean {
    val playAgain = input("Do you want to play again? (Y/n)").lowercase()
    return playAgain == "y" || playAgain == "yes" || playAgain.isEmpty()
}


fun printGreetings(vocabularyPath: String) = println("""🚀 Let's learn new words!
📖 The game is about matching words with their translations.
🔢 Enter indexes the words you think match with space between them.
📝 Enter '$exitCommand' to quit the game at any time.

📚 Loading vocabulary file: $vocabularyPath...""")

fun main(args: Array<String>) {
    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"
    printGreetings(vocabularyPath)
    val spreadsheet = Spreadsheet(vocabularyPath)
    val vocabulary = spreadsheet.loadVocabulary()
    println("✅ ${vocabulary.size} words loaded.\n")

    val roundsNumber: Int
    if (vocabulary.size < numberOfWordsInRound) {
        println("⚠️ Error: Not enough words to play the game. Add more words to the vocabulary file.")
        exitProcess(1)
    } else if (vocabulary.size < recommendedVocabularySize) {
        println("⚠️ Warning: The vocabulary is small. Consider adding more words to the vocabulary file.")
        roundsNumber = input("Enter the number of rounds (default: ${vocabulary.optimalNumberOfRounds()}):")
            .toIntOrNull() ?: vocabulary.optimalNumberOfRounds()
    } else {
        roundsNumber = input("Enter the number of rounds (default: 20):").toIntOrNull() ?: 20
    }
    var play = true
    while (play) {
        val game = Game(vocabulary, roundsNumber = roundsNumber)
        gameProcess(game)
        play = playAgain()
        println(
            "\n" + if (play) "🔄 Ok, let's play again!\n"
            else "🎮 Thank you for playing!\n$bye"
        )

    }
    spreadsheet.close()
}