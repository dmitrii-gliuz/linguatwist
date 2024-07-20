package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.localeString

object Menu {
    private val items = mapOf(
        "Start the game" to ::startGame,
        "Exit" to ::exitGameWrapper
    )

    private fun show() {
        val keys = items.keys.mapIndexed { index, line -> "${index + 1}. $line" }
        println(keys.drawFrame(caption = "Main menu"))
    }

    private fun inputIndex(): Int {
        while (true) {
            val index = input("${localeString("EnterTheNumber")}:").toIntOrNull()
            if (index != null && index in 1..items.size) return index
            println("⚠️ ${localeString("Error")}: ${localeString("EnterTheCorrectNumber")}.")
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
            val index = inputIndex()
            selectItem(index)
        }
    }
}