package org.dmitriigliuz.learnnewwords

import kotlin.system.exitProcess

fun input(message: String): String {
    print("👉 $message ")
    val userInput = readLine() ?: ""
    if (userInput == "exit") {
        println("👋 Bye!")
        exitProcess(0)
    }
    return userInput
}

fun inputPairIndexes(): List<Int> {
    val userInput = input("Enter indexes for matching words:")
    val indexes = userInput.split("""\s+""".toRegex())
    return indexes.map { it.toInt() - 1 }
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

fun gameProcess(game: Game) {
    while (!game.isFinished()) {
        print(outputRound(game.currentRound()))
        println("🌟Score: ${game.score}\t❌Mistakes: ${game.mistakes}\n")
        game.enterIndexesAndPrintResult()
    }
    println(if (game.isFailed) {
        "😢 Sorry, you have made three mistakes and failed the game!"
    } else {
        "🎉 Congratulations! You have finished the game!"
    })
    val (percent, score) = game.getPercentageAndVerbalScore()
    println("🎯 Your result: $percent%\n🏆 $score\n")
}



fun main(args: Array<String>) {
    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"
    println("🚀 Let's learn new words!\n" +
            "📖 The game is about matching words with their translations.\n" +
            "🔢 Enter indexes the words you think match with space between them.\n" +
            "📝 Enter 'exit' to quit the game at any time.\n" +
            "\n📚 Loading vocabulary file: $vocabularyPath...")
    val spreadsheet = Spreadsheet(vocabularyPath)
    val vocabulary = spreadsheet.loadVocabulary()
    println("✅ ${vocabulary.size} words loaded.\n")
    var play = true
    while (play) {
        val game = Game(vocabulary)
        gameProcess(game)
        val playAgain = input("Do you want to play again? (Y/n)").lowercase()
        play = playAgain == "y" || playAgain == "yes" || playAgain.isEmpty()
        println(
            "\n" + if (play) "🔄 Ok, let's play again!\n"
            else "🎮 Thank you for playing! 👋 Bye!"
        )

    }
    spreadsheet.close()
}