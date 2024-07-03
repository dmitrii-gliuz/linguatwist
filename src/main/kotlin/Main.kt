package org.dmitriigliuz.learnnewwords

import org.dmitriigliuz.learnnewwords.console.playConsoleGame
import org.dmitriigliuz.learnnewwords.console.printGreetings
import org.dmitriigliuz.learnnewwords.core.Spreadsheet

fun main(args: Array<String>) {
    playConsoleGame(args)
//    val vocabularyPath = if (args.isNotEmpty()) args[0] else "vocabulary.xlsx"
//    val spreadsheet = Spreadsheet(vocabularyPath)
//    val vocabulary = spreadsheet.loadVocabulary()
//    spreadsheet.writeScores(vocabulary)
}