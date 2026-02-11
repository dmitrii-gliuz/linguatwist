package org.dmitriigliuz.linguatwist.legacy.presentation.cli

fun StringBuilder.space(): java.lang.StringBuilder = append(" ")

fun StringBuilder.newLine(): java.lang.StringBuilder = append("\n")

fun StringBuilder.appendLine(line: String): java.lang.StringBuilder = append(line).newLine()

inline fun printbs(buildAction: StringBuilder.() -> Unit) {
    println(StringBuilder().apply(buildAction).toString())
}