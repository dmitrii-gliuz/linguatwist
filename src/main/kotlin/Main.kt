package org.dmitriigliuz.learnnewwords

fun input(message: String): String {
    print("$message ")
    return readln()
}

fun inputPairIndexes(): List<Int> {
    val userInput = input("Enter indexes for matching words:")
    val indexes = userInput.split("""\s""".toRegex())
    return indexes.map { it.toInt() - 1 }
}

private fun Game.enterIndexesAndPrintResult() {
    val indexes = inputPairIndexes()
    val (result, words) = choosePair(indexes)
    val (firstWord, secondWord) = words
    println("Your choice:\n" +
            "${indexes[0] + 1}. $firstWord\n" +
            "${indexes[1] + 1}. $secondWord\n")
    val printingResult = if (result) "Right" else "Wrong"
    println("$printingResult!\n")
}

fun gameProcess(game: Game) {
    val horizontalLine = "=".repeat(35)
    while (!game.isFinished()) {
        println("Round ${game.currentRoundNumber}" +
                (if (game.mistakesInRound() > 0) " (Mistakes: ${game.mistakesInRound()})" else "") + "\n" +
                horizontalLine +
                "\n${game.currentRound()}\n" +
                horizontalLine +
                "\nScore: 0\tMistakes: ${game.mistakes}\n"
        )
        game.enterIndexesAndPrintResult()
    }
    if (game.isFailed) {
        println("Sorry, you have made three mistakes and failed the game!")
    } else {
        println("Congratulations! You have finished the game!")
    }
}

fun main(args: Array<String>) {
    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"

    println("Let's learn new words!\n" +
            "Loading vocabulary file: $vocabularyPath")
    val spreadsheet = Spreadsheet(vocabularyPath)
    val vocabulary = spreadsheet.loadVocabulary()
    println("${vocabulary.size} words loaded.\n")
    var play = true
    while (play) {
        val game = Game(vocabulary)
        gameProcess(game)
        val playAgain = input("Do you want to play again? (Y/n)").lowercase()
        play = playAgain == "y" || playAgain == "yes" || playAgain.isEmpty()
        println(
            "\n" + if (play) "Ok, let's play again!\n"
            else "Thank you for playing! Bye!"
        )

    }
    spreadsheet.close()
}