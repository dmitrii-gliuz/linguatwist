package org.dmitriigliuz.learnnewwords.console

import org.dmitriigliuz.learnnewwords.core.*
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

fun getFormattedResult(result: Boolean) = buildString {
    append(if (result) "✅ Right!" else "❌ Wrong!")
    newLine()
    append(if (result) "🔼 +1" else "🔽 -1")
}

fun printGreetings(vocabularyPath: String) = println("""🚀 Let's learn new words!
📖 The game is about matching words with their translations.
🔢 Enter indexes the words you think match with space between them.
📝 Enter '$exitCommand' to quit the game at any time.

📚 Loading vocabulary file: $vocabularyPath...""")


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
    spreadsheet.writeScores(vocabulary)
    println("📝 Scores saved to $vocabularyPath.")
    spreadsheet.close()
}

fun Vocabulary.printStatistics() {
    wordPairs.forEach {
        println("${it.originalWord} - ${it.translation} - ${it.score}")
    }
}