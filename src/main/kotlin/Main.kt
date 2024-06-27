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

fun main(args: Array<String>) {
    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"

    println("Let's learn new words!\n" +
            "Loading vocabulary file: $vocabularyPath")

    val vocabulary = SpreadsheetReader(vocabularyPath).load()
    val game = Game(vocabulary)
    println("${game.vocabularySize} words loaded.\n")
    val horizontalLine = "=".repeat(35)
    while (!game.isFinished()) {
        println("Round ${game.currentRoundNumber}\n" +
                horizontalLine +
                "\n${game.currentRound()}\n" +
                horizontalLine
        )
        game.enterIndexesAndPrintResult()
    }

}