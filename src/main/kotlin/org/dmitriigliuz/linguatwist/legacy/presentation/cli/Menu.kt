package org.dmitriigliuz.linguatwist.legacy.presentation.cli

import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext

class Menu(private val gameSession: GameSession,
    private val context: AppContext
) {


    private val items = mapOf(
        context.getString("StartTheGame") to { gameSession.startGame() },
        context.getString("Exit") to { gameSession.exitGame() }
    )

    private fun show() = println(
        items.keys.mapIndexed { index, line -> "${index + 1}. $line" }
            .drawFrame(caption = context.getString("MainMenu"))
    )

    private fun inputIndex(): Int {
        while (true) {
            val index = gameSession.input("EnterMenuItemNumber").toIntOrNull()
            if (index != null && index in 1..items.size) return index
            println(context.getString("IncorrectNumberOfMenuItem"))
        }
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