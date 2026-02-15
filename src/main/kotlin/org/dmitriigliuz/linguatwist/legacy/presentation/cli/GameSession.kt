package org.dmitriigliuz.linguatwist.legacy.presentation.cli

import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext
import org.dmitriigliuz.linguatwist.legacy.application.coordinator.GameOrchestrator
import org.dmitriigliuz.linguatwist.game.GameEngine
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji.Emoji
import org.dmitriigliuz.linguatwist.legacy.Spreadsheet
import kotlin.system.exitProcess

// TODO Fix error printing

class GameSession(private val context: AppContext) {
    private val spreadsheet = Spreadsheet(context)
    private val maxStringLengthsInRounds = mutableListOf<Int>()
    private val ui = UIDelegate(context)
    private val exitCommand = "exit"
    private val skipCommand = "skip"
    private val logo = """
╭───────────────────────────────────────────────────────────────────────────────────────╮
│ ██      ██ ███    ██  ██████  ██    ██  █████  ████████ ██     ██ ██ ███████ ████████ │
│ ██      ██ ████   ██ ██       ██    ██ ██   ██    ██    ██     ██ ██ ██         ██    │
│ ██      ██ ██ ██  ██ ██   ███ ██    ██ ███████    ██    ██  █  ██ ██ ███████    ██    │
│ ██      ██ ██  ██ ██ ██    ██ ██    ██ ██   ██    ██    ██ ███ ██ ██      ██    ██    │
│ ███████ ██ ██   ████  ██████   ██████  ██   ██    ██     ███ ███  ██ ███████    ██    │
╰───────────────────────────────────────────────────────────────────────────────────────╯                                                                                    

""".trimMargin()

    fun start() {
        println(logo)
        if (!context.isCurrentLanguageSupported()) {
            println(
                context.messageBuilder.warning(ui.text("TheLanguageIsNotAvailable"))
            )
        }
        printbs {
        append(ui.welcome("Welcome"))
            .append("! ")
            .appendLine(ui.text("LetsLearnWords"))
            .appendLine(ui.emojiText(Emoji.GAME_RULES, "TheGameIsAboutMatchingWords"))
            .appendLine(ui.emojiText(Emoji.ENTER_INDEXES, "EnterTheIndexesOfWordsYouThinkMatch"))
            .appendLine(ui.emojiText(Emoji.GAME_DESCRIPTION, "EnterExitToQuitAtAnyTime"))
    }
        Menu(this, context).navigate()
    }

    fun writeScoresSafely(spreadsheet: Spreadsheet, vocabulary: Vocabulary): Boolean {
        while (true) {
             try {
                spreadsheet.writeScores(vocabulary)
                return true
            } catch (e: Exception) {
                println(context.messageBuilder.error(e.message ?: ui.text("UnexpectedError")))
                input("EnterSkipOrExit").also { if (it == skipCommand) return false }
            }
        }
    }

    fun printErrorAndTryAgain(e: Exception? = null, text: String = "") {
        val message = text.ifEmpty { e?.message ?: ui.text("UnexpectedError") }
        println(context.messageBuilder.error("${ui.text("Error")}: $message."))
        println(ui.text("TryAgain"))
        println()
    }

    fun startGame() {
        val spreadsheetPath = context.config.spreadsheetPath
        println("${ui.emojiText(Emoji.LOADING, "LoadingVocabularyFile")}: $spreadsheetPath...")
        val gameOrchestrator = GameOrchestrator(context, spreadsheet)
        val vocabulary = gameOrchestrator.vocabulary

        printNumberOfLoadedWords(vocabulary, spreadsheet)

        var play = true
        while (play) {
            val game = try {
                val numberOfRounds = setNumberOfRounds(spreadsheet)
                gameOrchestrator.newGame(numberOfRounds)
            } catch (e: IllegalArgumentException) {
                printErrorAndTryAgain(e)
                continue
            } catch (e: org.dmitriigliuz.linguatwist.legacy.storage.StorageException) {
                exitGame(e.message ?: ui.text("UnexpectedError"))
                throw e
            } catch (e: Exception) {
                exitGame(ui.text("UnexpectedError"))
                throw e
            }
            maxStringLengthsInRounds.clear()
            while (!game.isFinished()) {
                addMaxLengthOfRound(game)
                printCurrentRoundAndGameState(game)
                enterIndexesAndPrintResult(game)
            }
            val spentTime = game.spentTime()
            printCurrentRoundAndGameState(game)
            showGameResults(game, spentTime)

            val userAnswer = input("DoYouWantToPlayAgain ", "(Y/n)").lowercase()
            play = userAnswer in listOf("y", "yes", "")
            val scoresSaved = writeScoresSafely(spreadsheet, vocabulary)
            println(responseToPlayAgainAnswer(play, scoresSaved))
        }
    }

    fun progressBar(game: GameEngine): String {
        val progressBarLength = 20
        val square = ui.emoji(Emoji.PROGRESS)
        val emptySquare = ui.emoji(Emoji.PROGRESS_EMPTY)
        val bar: String
        val percents = game.completionPercentage()
        bar = when (percents) {
            100 -> square.repeat(progressBarLength)
            0 -> emptySquare.repeat(progressBarLength)
            !in 0..100 -> throw IllegalArgumentException(ui.text("PercentageShouldBeInRange"))
            else -> {
                val progress = percents * progressBarLength / 100
                buildString { append(square.repeat(progress))
                    append(emptySquare.repeat(progressBarLength - progress))
                }
            }
        }
        val useEmojis = context.config.useEmojis
        return buildString {
            if (!useEmojis) append("[")
            append(bar)
            if (!useEmojis) append("]")
            space()
            append("$percents%")
        }
    }

    fun exitGame(messageText: String = "") {
        if (messageText.isNotEmpty()) {
            println("${ui.emoji(Emoji.ERROR)}${ui.text("Error")}: $messageText.")
        }
        println(ui.bye("Bye"))
        exitProcess(0)
    }

    fun input(messageKey: String, additionalText: String = ""): String {
        printbs{
            append(ui.emojiText(Emoji.ENTER, messageKey))
            if (additionalText.isNotEmpty()) append(additionalText)
            append(": ")
        }
        val userInput = readlnOrNull()?.trim() ?: ""
        if (userInput == exitCommand) exitGame()
        return userInput
    }

    fun inputIndexes(): Pair<Int, Int> {
        val indexes = input("EnterIndexesForMatchingWords")
            .split("""\s+""".toRegex())
            .map { it.toInt() - 1 }
        return indexes[0] to indexes[1]
    }

    fun addMaxLengthOfRound(game: GameEngine) {
        if (maxStringLengthsInRounds.size < game.currentRound.number) {
            maxStringLengthsInRounds.add(getRoundOutputLines(game.currentRound).maxOf { it.length })
        }
    }

    fun getRoundOutputLines(round: Round): List<String> = buildList {
        val maxLength = round.pairs.maxOf { it.sourceText.length } + 3
        for (numberOfItem in 1..context.config.pairsPerRound) {
            val index = numberOfItem - 1
            val leftPair = round.pairs[index]
            val rightPair = round.pairs[round.rightColumnPermutation[index]]
            val firstWordOutput = leftPair.run { applyStrikethroughIfGuessed(sourceText) }
            val secondWordOutput = rightPair.run { applyStrikethroughIfGuessed(translation) }
            val spaces = " ".repeat(maxLength - leftPair.sourceText.length)

            add("$numberOfItem. $firstWordOutput$spaces$numberOfItem. $secondWordOutput")
        }
    }

    fun getRoundCard(round: Round): String {
        val bottomCaption = if (round.mistakesCount > 0) "${ui.text("MistakesInRound")}: ${round.mistakesCount}" else ""
        val caption = "${ui.text("Round")} ${round.number}"
        val maxLengthOfString = if (maxStringLengthsInRounds.size >= round.number) {
            maxStringLengthsInRounds[round.index]
        } else -1
        return getRoundOutputLines(round).drawFrame(caption, bottomCaption, maxLengthOfString)
    }

    fun printCurrentRoundAndGameState(game: GameEngine) = printbs {
        with (game) {
            appendLine(getRoundCard(currentRound))
            append(ui.emojiText(Emoji.SCORES_SAVED, "Score"))
            append(": $score  ")
            append(ui.emojiText(Emoji.MISTAKES, "Mistakes"))
            appendLine(": $mistakes")
//            TODO Use progress bar from message builder and remove it from here
//            appendLine(context.messageBuilder.progressBar())
        appendLine(progressBar(game))
        }
    }

    fun formattedResult(result: Boolean) = buildString {
        val resultMessage : String
        val pointsMessage: String
        if (result) {
            resultMessage = ui.emojiText(Emoji.RIGHT, "Right")
            pointsMessage = "${ui.emoji(Emoji.POINT_UP)}+1"
        } else {
            resultMessage = ui.emojiText(Emoji.WRONG, "Wrong")
            pointsMessage = "${ui.emoji(Emoji.POINT_DOWN)}-1"
        }

    appendLine("$resultMessage!")
        append(pointsMessage)
        appendLine(" ${ui.text("Point")}.")
    }

    fun choiceResult(game: GameEngine, indexes: List<Int>, words: List<String>, result: Boolean) = buildString {
        val (first, second) = words
        appendLine("${ui.text("YourChoice")}:")
        appendLine("${indexes[0] + 1}. $first")
        appendLine("${indexes[1] + 1}. $second")
        if (game.currentRound.isFinished()) {
            appendLine(getRoundCard(game.currentRound))
        }
        appendLine(formattedResult(result))
    }

    fun enterIndexesAndPrintResult(game: GameEngine) {
        var finished = false
        while (!finished) {
            val indexes = try {
                inputIndexes()
            } catch (e: NumberFormatException) {
                printErrorAndTryAgain(e, ui.text("InputShouldContainOnlyTwoNumbers"))
                continue
            }
            val (result, words) = try {
                game.processAnswer(indexes)
            } catch (e: IllegalArgumentException) {
                printErrorAndTryAgain(e)
                continue
            }
            println(choiceResult(game, indexes, words, result))
            finished = true
        }
    }

    fun showGameResults(game: GameEngine, spentTime: String) = printbs {
        appendLine(
            if (game.isFailed) {
                "${ui.emoji(Emoji.SORRY)}${ui.text("SorryYouFailed")}"
            } else {
                "${ui.emoji(Emoji.CONGRATULATIONS)}${ui.text("CongratulationsYouFinished")}"
            }
        )
        val (percent, score) = game.percentageAndVerbalScore()
        appendLine("${ui.emoji(Emoji.YOUR_RESULT)}${ui.text("YourResult")}: $percent%")
        appendLine(
            if (context.config.useEmojis) {
                "${score.emoji} ${ui.text(score.description)} ${score.emoji}"
            } else {
                ui.text(score.description)
            }
        )
        appendLine("${ui.emoji(Emoji.COMPLETION_TIME)}${ui.text("CompletionTime")}: $spentTime")
    }

    fun setNumberOfRounds(spreadsheet: Spreadsheet): Int {
        val defaultValue = spreadsheet.defaultNumberOfRounds
        return input(
            "EnterTheNumberOfRounds",
            "(${ui.text("Default")}: $defaultValue)"
        ).toIntOrNull() ?: defaultValue
    }

    fun printNumberOfLoadedWords(vocabulary: Vocabulary, spreadsheet: Spreadsheet) = printbs {
// TODO Use UI Delegate for emojis
        var warnings = false
        fun warningIf(condition: Boolean, message: String) {
            if (condition) {
                warnings = true
                appendLine(context.messageBuilder.warning(message))
            }
        }
        append(ui.emojiText(Emoji.READY, "TheNumberOfLoadedWords"))
        appendLine(": ${vocabulary.size}")
        warningIf(vocabulary.size < context.config.recommendedVocabularySize,
            ui.text("TheVocabularyIsSmall"))
        warningIf(spreadsheet.duplicates > 0,
            "${ui.text("NumberOfDuplicates")}: ${spreadsheet.duplicates}")
        warningIf(spreadsheet.pairsWithSameWords > 0,
            "${ui.text("NumberOfPairsWithSameWords")}: ${spreadsheet.pairsWithSameWords}")
        warningIf(spreadsheet.incorrectRows > 0,
            "${ui.text("IncorrectRows")}: ${spreadsheet.incorrectRows}")
        if (warnings) appendLine("${ui.emojiText(Emoji.CHECK_VOCABULARY, "CheckTheVocabularyFile")}.")
            .newLine()
        append(ui.emojiText(Emoji.ROUNDS, "TheNumberOfRoundsToCoverAllWords"))
        appendLine(": ${spreadsheet.numberOfRoundsToCoverAllWords}.")
    }

    fun responseToPlayAgainAnswer(play: Boolean, scoresSaved: Boolean) = buildString {
        if (scoresSaved) {
            appendLine("${ui.emojiText(Emoji.SCORES_SAVED, "ScoresSavedTo")} ${context.config.spreadsheetPath}.")
                .newLine()
        }
        append(
            if (play) {
                ui.emojiText(Emoji.LETS_PLAY_AGAIN, "OKLetsPlayAgain")
            } else {
                ui.emojiText(Emoji.THANK_YOU, "ThankYouForPlaying")
            }
        )
        appendLine("!")
    }
}