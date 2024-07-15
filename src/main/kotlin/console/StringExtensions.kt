package org.dmitriigliuz.linguatwist.console

fun String.strikethrough() = "\u001B[9m$this\u001B[0m"

fun String.withoutStrikethrough(): String =
    if (this.contains("\u001B[9m") && this.contains("\u001B[0m")) {
        this.replace("\u001B[9m", "").replace("\u001B[0m", "")
    } else this

fun StringBuilder.newLine(): java.lang.StringBuilder = append("\n")

fun StringBuilder.space(): java.lang.StringBuilder = append(" ")

enum class FrameChars(val char: Char) {
    TOP_LEFT('┌'),
    TOP_RIGHT('┐'),
    BOTTOM_LEFT('└'),
    BOTTOM_RIGHT('┘'),
    HORIZONTAL('─'),
    VERTICAL('│')
}

fun String.padLeftRight(totalLength: Int, padChar: Char = ' ') = buildString {
    val thisString = this@padLeftRight
    val padding = totalLength - (thisString.length + 2)
    val leftPadding = padding / 2
    val rightPadding = padding - leftPadding
    append(padChar.toString().repeat(leftPadding))
    append(" $thisString ")
    append(padChar.toString().repeat(rightPadding))
}

fun String.padEnd(totalLength: Int, padChar: Char = ' ') = buildString {
    val thisString = this@padEnd
    val padding = totalLength - thisString.withoutStrikethrough().length
    append(thisString)
    if (padding > 0) append(padChar.toString().repeat(padding))
}

fun List<String>.drawFrame(caption: String = "", bottomCaption: String = "", maxStringLength: Int = -1) = buildString {
    val thisList = this@drawFrame
    val lengths = buildList {
        add(caption.length)
        add(bottomCaption.length)
        if (maxStringLength > 0) add(maxStringLength) else addAll(thisList.map { it.length })
    }
    val frameLength = lengths.max() + 2
    append(FrameChars.TOP_LEFT.char)
    fun drawCaption(text: String) = append(
        if (text.isNotEmpty()) {
            text.padLeftRight(frameLength, FrameChars.HORIZONTAL.char)
        } else {
            FrameChars.HORIZONTAL.char.toString().repeat(frameLength)
        }
    )
    drawCaption(caption)
    append(FrameChars.TOP_RIGHT.char)
    thisList.forEach {
        newLine()
        append(FrameChars.VERTICAL.char)
        space()
        append(it.padEnd(frameLength - 2))
        space()
        append(FrameChars.VERTICAL.char)
    }
    newLine()
    append(FrameChars.BOTTOM_LEFT.char)
    drawCaption(bottomCaption)
    append(FrameChars.BOTTOM_RIGHT.char)
}