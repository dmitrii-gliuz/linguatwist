package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.*
import kotlin.system.exitProcess


const val exitCommand = "exit"
const val skipCommand = "skip"


val bye = "👋 ${message("Bye")}"


fun exit(messageText: String = "") {
    if (messageText.isNotEmpty()) println("⛔️ $messageText")
    println(bye)
    exitProcess(0)
}

fun input(messageText: String): String {
    print("👉 $messageText ")
    val userInput = readln().trim()
    if (userInput == exitCommand) exit()
    return userInput
}

fun setNumberOfRounds(vocabulary: Vocabulary): Int {
    if (vocabulary.size < numberOfWordsInRound) {
        exit("⚠️ ${message("Error")}: ${message("NotEnoughWordsToPlayTheGame")}")
    }
    val defaultValue = if (vocabulary.size < recommendedVocabularySize) {
        println("⚠️ ${message("Warning")}: ${message("TheVocabularyIsSmall")}")
        vocabulary.optimalNumberOfRounds
    } else defaultNumberOfRounds
    return input("${message("EnterTheNumberOfRounds")} (${message("Default")}: $defaultValue):")
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
    append("$roundProgressEmoji ${message("Round")} ${this@getFormattedOutput.number}")
    if (this@getFormattedOutput.mistakes > 0) append(" (${message("Mistakes")}: ${this@getFormattedOutput.mistakes})")
    newLine()
    append(horizontalLine)
    append(this@getFormattedOutput.toString())
    append(horizontalLine)
}

fun getFormattedResult(result: Boolean) = buildString {
    append(if (result) "✅ ${message("Right")}!" else "❌ ${message("Wrong")}!")
    newLine()
    append(if (result) "🔼 +1" else "🔽 -1")
    append(" ${message("Point")}.")
    newLine()
}

fun printGreetings(vocabularyPath: String) = println("""🚀 ${message("Welcome")}! ${message("LetsLearnWords")}!
📖 ${message("TheGameIsAboutMatchingWords")}
🔢 ${message("EnterTheIndexesOfWordsYouThinkMatch")}
📝 ${message("EnterExitToQuitAtAnyTime")}

📚 ${message("LoadingVocabularyFile")}: $vocabularyPath...""")


fun Vocabulary.printNumberOfLoadedWords() = println("✅ ${message("TheNumberOfLoadedWords")}: $size.\n")

fun Spreadsheet.writeScoresSafely(vocabulary: Vocabulary) {
    while (true) {
        try {
            writeScores(vocabulary)
            break
        } catch (e: Exception) {

            val action = input("⚠️ ${message("Error")}: ${e.message}.\n" +
                    "${message("EnterSkipOrExit")}:")
            if (action == skipCommand) break

        }
    }
}

fun playConsoleGame(vocabularyPath: String) {
    printGreetings(vocabularyPath)
//    try {
//        val spreadsheet = Spreadsheet(vocabularyPath)
//    }
    val spreadsheet = Spreadsheet(vocabularyPath)
    val vocabulary = spreadsheet.loadVocabulary()
    vocabulary.printNumberOfLoadedWords()
    var play = true
    while (play) {
        val numberOfRounds = setNumberOfRounds(vocabulary)
        val game = Game(vocabulary, numberOfRounds = numberOfRounds)
        game.run()
        play = input("${message("DoYouWantToPlayAgain")} (Y/n)").lowercase() in listOf("y", "yes", "")
        spreadsheet.writeScoresSafely(vocabulary)
        println("📝 ${message("ScoresSavedTo")} $vocabularyPath.")
        println( "\n" + if (play) "🔄 ${message("OKLetsPlayAgain")}!\n" else "🎮 ${message("ThankYouForPlaying")}!\n$bye" )
    }
    spreadsheet.close()
}