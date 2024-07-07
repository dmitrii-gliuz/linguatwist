package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.*
import kotlin.system.exitProcess


const val exitCommand = "exit"
const val skipCommand = "skip"


val bye = "👋 ${getLocaleMessage("Bye")}"

fun errorMessage(message: String) = "⚠️ ${getLocaleMessage("Error")}: $message.\n"


fun exit(messageText: String = "") {
    if (messageText.isNotEmpty()) println("⛔️ ${getLocaleMessage("Error")} $messageText")
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
        exit(getLocaleMessage("NotEnoughWordsToPlayTheGame"))
    }
    val defaultValue = if (vocabulary.size < recommendedVocabularySize) {
        println("⚠️ ${getLocaleMessage("Warning")}: ${getLocaleMessage("TheVocabularyIsSmall")}")
        vocabulary.optimalNumberOfRounds
    } else defaultNumberOfRounds
    return input("${getLocaleMessage("EnterTheNumberOfRounds")} (${getLocaleMessage("Default")}: $defaultValue):")
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
    append("$roundProgressEmoji ${getLocaleMessage("Round")} ${this@getFormattedOutput.number}")
    if (this@getFormattedOutput.mistakes > 0) append(" (${getLocaleMessage("Mistakes")}: ${this@getFormattedOutput.mistakes})")
    newLine()
    append(horizontalLine)
    append(this@getFormattedOutput.toString())
    append(horizontalLine)
}

fun getFormattedResult(result: Boolean) = buildString {
    append(if (result) "✅ ${getLocaleMessage("Right")}!" else "❌ ${getLocaleMessage("Wrong")}!")
    newLine()
    append(if (result) "🔼 +1" else "🔽 -1")
    append(" ${getLocaleMessage("Point")}.")
    newLine()
}

fun printGreetings(vocabularyPath: String) = println("""🚀 ${getLocaleMessage("Welcome")}! ${getLocaleMessage("LetsLearnWords")}!
📖 ${getLocaleMessage("TheGameIsAboutMatchingWords")}
🔢 ${getLocaleMessage("EnterTheIndexesOfWordsYouThinkMatch")}
📝 ${getLocaleMessage("EnterExitToQuitAtAnyTime")}

📚 ${getLocaleMessage("LoadingVocabularyFile")}: $vocabularyPath...""")


fun Vocabulary.printNumberOfLoadedWords() = println("✅ ${getLocaleMessage("TheNumberOfLoadedWords")}: $size.\n")

fun Spreadsheet.writeScoresSafely(vocabulary: Vocabulary) {
    while (true) {
        try {
            writeScores(vocabulary)
            break
        } catch (e: Exception) {

            val action = input("⚠️ ${getLocaleMessage("Error")}: ${e.message}.\n" +
                    "${getLocaleMessage("EnterSkipOrExit")}:")
            if (action == skipCommand) break

        }
    }
}

fun playConsoleGame(vocabularyPath: String) {
    printGreetings(vocabularyPath)
    val spreadsheet: Spreadsheet
    try {
        spreadsheet = Spreadsheet(vocabularyPath)
    } catch (e: SpreadsheetException) {
        exit(e.message ?: "")
        throw e
    }
    val vocabulary = spreadsheet.loadVocabulary()
    vocabulary.printNumberOfLoadedWords()
    var play = true
    while (play) {
        val numberOfRounds = setNumberOfRounds(vocabulary)
        val game = Game(vocabulary, numberOfRounds = numberOfRounds)
        game.run()
        play = input("${getLocaleMessage("DoYouWantToPlayAgain")} (Y/n)").lowercase() in listOf("y", "yes", "")
        spreadsheet.writeScoresSafely(vocabulary)
        println("📝 ${getLocaleMessage("ScoresSavedTo")} $vocabularyPath.")
        println( "\n" + if (play) "🔄 ${getLocaleMessage("OKLetsPlayAgain")}!\n" else "🎮 ${getLocaleMessage("ThankYouForPlaying")}!\n$bye" )
    }
    spreadsheet.close()
}