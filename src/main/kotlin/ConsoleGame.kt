package org.dmitriigliuz.learnnewwords

import kotlin.system.exitProcess

const val bye = "👋 Bye!"
const val exitCommand = "exit"

fun exit(message: String = "") {
    if (message.isNotEmpty()) println("⛔️ $message")
    println(bye)
    exitProcess(0)
}

fun input(message: String): String {
    print("👉 $message ")
    val userInput = readln().trim()
    if (userInput == exitCommand) exit()
    return userInput
}

fun setNumberOfRounds(vocabulary: Vocabulary): Int {
    if (vocabulary.size < numberOfWordsInRound) {
        exit("⚠️ Error: Not enough words to play the game. Add more words to the vocabulary file.")
    }
    val defaultValue = if (vocabulary.size < recommendedVocabularySize) {
        println("⚠️ Warning: The vocabulary is small. Consider adding more words to the vocabulary file.")
        vocabulary.optimalNumberOfRounds
    } else defaultNumberOfRounds
    return input("Enter the number of rounds (default: $defaultValue):")
        .toIntOrNull() ?: defaultValue
}

fun Round.getFormattedOutput() = buildString {
    val horizontalLine = buildString {
    append("=".repeat(35))
    newLine()
}
    val roundProgressEmoji = when (this@getFormattedOutput.guessedPairs) {
        0 -> "🔵" // Beginning of the round
        1 -> "🟢" // Early stage of the round
        2 -> "🟡" // Middle stage of the round
        3 -> "🟠" // Late stage of the round
        else -> "🔴" // End of the round
    }
    append("$roundProgressEmoji Round ${this@getFormattedOutput.number}")
    if (this@getFormattedOutput.mistakes > 0) append(" (Mistakes: ${this@getFormattedOutput.mistakes})")
    newLine()
    append(horizontalLine)
    append(this@getFormattedOutput.toString())
    append(horizontalLine)
}

fun Game.printCurrentRound() {
    val progressBarLength = 20
    val progressBar = buildString {
        val percents = completionPercentage()
        val progress = percents * progressBarLength / 100
        append("🌟Score: $score\t❌Mistakes: $mistakes")
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

fun getFormattedResult(result: Boolean) = buildString {
    append(if (result) "✅ Right!" else "❌ Wrong!")
    newLine()
    append(if (result) "🔼 +1" else "🔽 -1")
}

fun Game.enterIndexesAndPrintResult() {
    val indexes = input("Enter indexes for matching words:")
        .split("""\s+""".toRegex())
        .map { it.toInt() - 1 }
    val round = currentRound()
    try {
        val (result, words) = choosePair(indexes)
        val (firstWord, secondWord) = words
        println("Your choice:\n" +
                "${indexes[0] + 1}. $firstWord\n" +
                "${indexes[1] + 1}. $secondWord\n")
        if (round.isFinished()) println(previousRound().getFormattedOutput())
        println(getFormattedResult(result))
    } catch (e: IllegalArgumentException) {
        println("⚠️ Error: ${e.message}.\n   Try again!\n")
    }
}

fun printGreetings(vocabularyPath: String) = println("""🚀 Let's learn new words!
📖 The game is about matching words with their translations.
🔢 Enter indexes the words you think match with space between them.
📝 Enter '$exitCommand' to quit the game at any time.

📚 Loading vocabulary file: $vocabularyPath...""")


fun Game.run() {
    while (!isFinished()) {
        printCurrentRound()
        enterIndexesAndPrintResult()
    }
    printCurrentRound()
    println(if (isFailed) {
        "😢 Sorry, you have made three mistakes and failed the game!"
    } else {
        "🎉 Congratulations! You have finished the game!"
    })
    val (percent, score) = percentageAndVerbalScore()
    println("🎯 Your result: $percent%\n${score.emoji} ${score.description} ${score.emoji}\n")
}

fun Vocabulary.printLoadedWordsMessage() = println("✅ $size words loaded.\n")

fun playConsoleGame(args: Array<String>) {
    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"
    printGreetings(vocabularyPath)
    val spreadsheet = Spreadsheet(vocabularyPath)
    val vocabulary = spreadsheet.loadVocabulary()
    vocabulary.printLoadedWordsMessage()
    var play = true
    while (play) {
        val numberOfRounds = setNumberOfRounds(vocabulary)
        val game = Game(vocabulary, numberOfRounds = numberOfRounds)
        game.run()
        play = input("Do you want to play again? (Y/n)").lowercase() in listOf("y", "yes", "")
        println( "\n" + if (play) "🔄 Ok, let's play again!\n" else "🎮 Thank you for playing!\n$bye" )
    }
    spreadsheet.close()
}