package org.dmitriigliuz.linguatwist.legacy.presentation.cli


fun String.strikethrough() = "\u001B[9m$this\u001B[0m"

fun String.withoutStrikethrough(): String =
    if (contains("\u001B[9m") && contains("\u001B[0m")) {
        replace("\u001B[9m", "").replace("\u001B[0m", "")
    } else this

enum class FrameChars(val char: Char) {
    TOP_LEFT('┌'),
    TOP_RIGHT('┐'),
    BOTTOM_LEFT('└'),
    BOTTOM_RIGHT('┘'),
    HORIZONTAL('─'),
    VERTICAL('│')
}

fun String.padLeftRight(totalLength: Int, padChar: Char = ' ') = buildString {
    val padding = totalLength - (this@padLeftRight.length + 2)
    val leftPadding = padding / 2
    val rightPadding = padding - leftPadding
    append(padChar.toString().repeat(leftPadding))
    space().append(this@padLeftRight).space()
    append(padChar.toString().repeat(rightPadding))
}

fun String.padEnd(totalLength: Int, padChar: Char = ' ') = buildString {
    append(this@padEnd)
    (totalLength - withoutStrikethrough().length).run { if (this > 0) append(padChar.toString().repeat(this)) }
}

fun List<String>.drawFrame(caption: String = "", bottomCaption: String = "", maxStringLength: Int = -1) = buildString {
    val thisList = this@drawFrame
    val lengths = buildList {
        if (caption.isNotEmpty()) add(caption.length)
        if (bottomCaption.isNotEmpty()) add(bottomCaption.length)
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
    appendLine(FrameChars.TOP_RIGHT.char)
    thisList.forEach {
        append(FrameChars.VERTICAL.char)
        space().append(it.padEnd(frameLength - 2)).space()
        appendLine(FrameChars.VERTICAL.char)
    }
    append(FrameChars.BOTTOM_LEFT.char)
    drawCaption(bottomCaption)
    append(FrameChars.BOTTOM_RIGHT.char)
}