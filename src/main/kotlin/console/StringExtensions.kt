package org.dmitriigliuz.linguatwist.console

fun String.strikethrough() = "\u001B[9m$this\u001B[0m"

fun StringBuilder.newLine(): java.lang.StringBuilder = append("\n")