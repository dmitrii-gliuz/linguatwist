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

fun enterIndexesAndPrintResult(round: Round) {
    val indexes = inputPairIndexes()
    val result = round.choosePair(indexes)
    val (firstWord, secondWord) = round.getTwoWordsByIndexes(indexes)
    println("Your choice:\n" +
            "${indexes[0] + 1}. $firstWord\n" +
            "${indexes[1] + 1}. $secondWord\n")
    val printingResult = if (result) "Right" else "Wrong"
    println("$printingResult!\n")
}

fun main() {
    val vocabularyPath = "vocabulary.xlsx"
    println("Let's learn new words!\n" +
            "Loading vocabulary file: $vocabularyPath")
    val vocabulary = VocabularyLoader(vocabularyPath).load()
    val game = Game(vocabulary)
    println("${game.vocabularySize} words loaded.\n")
    val numberOfRounds = input("How many rounds do you want to play?").toInt()
    game.start(numberOfRounds)

    for (round in 1..roundsNumber) {
        println("Round $round!")
        game.setNewPairsGroup()
        val twoColumns = game.getTwoColumns()
        while (!twoColumns.solved()){
            println(twoColumns)
            enterIndexesAndPrintResult(twoColumns)
        }
        println("Solved")
    }

}