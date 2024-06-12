package org.dmitriigliuz.learnnewwords

fun String.strikethrough() = "\u001B[9m$this\u001B[0m"

fun input(message: String): String {
    print("$message ")
    return readln()
}

fun inputPairIndexes(): List<Int> {
    val userInput = input("Enter indexes for matching words:")
    val indexes = userInput.split("""\s""".toRegex())
    return indexes.map { it.toInt() - 1 }
}

fun enterIndexesAndPrintResult(twoColumns: TwoColumns) {
    val indexes = inputPairIndexes()
    val result = twoColumns.choosePair(indexes)
    val (firstWord, secondWord) = twoColumns.getTwoWordsByIndexes(indexes)
    println("Your choice:\n" +
            "${indexes[0] + 1}. $firstWord\n" +
            "${indexes[1] + 1}. $secondWord\n")
    val printingResult = if (result) "Right" else "Wrong"
    println("$printingResult!\n")
}

fun main() {
    val game = Game("vocabulary.xlsx")
    game.setNewFivePairs()
    val twoColumns = game.getTwoColumns()
    while (true){
        println(twoColumns)
        enterIndexesAndPrintResult(twoColumns)
    }
}