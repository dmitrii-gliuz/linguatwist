package org.dmitriigliuz.learnnewwords

fun String.strikethrough() = "\u001B[9m$this\u001B[0m"

fun StringBuilder.newLine() = append("\n")