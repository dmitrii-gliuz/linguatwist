package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.localeString

object Menu {
    private val items = mapOf(
        localeString("StartTheGame") to ::startGame,
        localeString("Exit") to ::exitGameWrapper
    )

    private fun show() = println(
        items.keys.mapIndexed { index, line -> "${index + 1}. $line" }.drawFrame(caption = localeString("MainMenu"))
    )

    private fun inputIndex(): Int {
        while (true) {
            val index = input("${localeString("EnterMenuItemNumber")}:").toIntOrNull()
            if (index != null && index in 1..items.size) return index
            printError(text = localeString("IncorrectNumberOfMenuItem"))
        }
    }

    private fun exitGameWrapper() {
        exitGame()
    }

    private fun selectItem(index: Int) {
        val itemName = items.keys.elementAt(index - 1)
        val item = items[itemName] ?: return
        item.invoke()
    }

    fun navigate() {
        while (true) {
            show()
            selectItem(inputIndex())
        }
    }
}