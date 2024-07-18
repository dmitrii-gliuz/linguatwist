package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.*
import kotlin.system.exitProcess


const val exitCommand = "exit"
const val skipCommand = "skip"

val bye = "👋 ${localeString("Bye")}"

val maxStringLengthsInRounds = mutableListOf<Int>()

fun exit(messageText: String = "") {
    if (messageText.isNotEmpty()) println("⛔️ ${localeString("Error")}: $messageText.")
    println(bye)
    exitProcess(0)
}

fun input(messageText: String): String {
    print("👉 $messageText ")
    val userInput = readln().trim()
    if (userInput == exitCommand) exit()
    return userInput
}


fun setNumberOfRounds(spreadsheet: Spreadsheet): Int {
    val defaultValue = spreadsheet.defaultNumberOfRounds
    return input(
        "${localeString("EnterTheNumberOfRounds")} (${localeString("Default")}: $defaultValue):"
    ).toIntOrNull() ?: defaultValue
}

fun getFormattedResult(result: Boolean) = buildString {
    append(if (result) "✅ ${localeString("Right")}!" else "❌ ${localeString("Wrong")}!")
    newLine()
    append(if (result) "🔼 +1" else "🔽 -1")
    append(" ${localeString("Point")}.")
    newLine()
}

fun printGreetings(vocabularyPath: String) = println("""🚀 ${localeString("Welcome")}! ${localeString("LetsLearnWords")}!
📖 ${localeString("TheGameIsAboutMatchingWords")}
🔢 ${localeString("EnterTheIndexesOfWordsYouThinkMatch")}
📝 ${localeString("EnterExitToQuitAtAnyTime")}

📚 ${localeString("LoadingVocabularyFile")}: $vocabularyPath...""")


fun printNumberOfLoadedWords(vocabulary: Vocabulary, spreadsheet: Spreadsheet) = println(
    buildString {
        var warnings = false
        fun warningIf(condition: Boolean, message: String) {
            if (condition) {
                warnings = true
                append("⚠️ ${localeString("Warning")}: $message.")
                newLine()
            }
        }
        append("✅ ${localeString("TheNumberOfLoadedWords")}: ${vocabulary.size}")
        newLine()
        warningIf(vocabulary.size < recommendedVocabularySize, localeString("TheVocabularyIsSmall"))
        warningIf(spreadsheet.duplicates > 0,
            "${localeString("NumberOfDuplicates")}: ${spreadsheet.duplicates}")
        warningIf(spreadsheet.pairsWithSameWords > 0,
            "${localeString("NumberOfPairsWithSameWords")}: ${spreadsheet.pairsWithSameWords}")
        if (warnings) {
            append("🔍 ${localeString("CheckTheVocabularyFile")}.")
            repeat(2) { newLine() }
        }
        append("🔤 ${localeString("TheNumberOfRoundsToCoverAllWords")}: ${spreadsheet.numberOfRoundsToCoverAllWords}.")
        newLine()
    }
)

fun Spreadsheet.writeScoresSafely(vocabulary: Vocabulary) {
    while (true) {
        try {
            writeScores(vocabulary)
            break
        } catch (e: Exception) {
            val action = input("⚠️ ${localeString("Error")}: ${e.message}.\n" +
                    "${localeString("EnterSkipOrExit")}:")
            if (action == skipCommand) break
        }
    }
}

fun playConsoleGame(vocabularyPath: String) {
    printGreetings(vocabularyPath)
    val spreadsheet: Spreadsheet
    val vocabulary = try {
        spreadsheet= Spreadsheet(vocabularyPath)
        spreadsheet.loadVocabulary()
    } catch (e: SpreadsheetException) {
        exit(e.message ?: "")
        throw e
    }
    printNumberOfLoadedWords(vocabulary, spreadsheet)
    var play = true
    while (play) {
        val game = try {
            val numberOfRounds = setNumberOfRounds(spreadsheet)
            Game(vocabulary, numberOfRounds = numberOfRounds)
        } catch (e: IllegalArgumentException) {
            println("⚠️ ${localeString("Error")}: ${e.message}\n   ${localeString("TryAgain")}!\n")
            continue
        }
        game.run()
        play = input("${localeString("DoYouWantToPlayAgain")} (Y/n)").lowercase() in listOf("y", "yes", "")
        spreadsheet.writeScoresSafely(vocabulary)
        println("📝 ${localeString("ScoresSavedTo")} $vocabularyPath.")
        println( "\n" + if (play) "🔄 ${localeString("OKLetsPlayAgain")}!\n" else "🎮 ${localeString("ThankYouForPlaying")}!\n$bye" )
    }
    spreadsheet.close()
}