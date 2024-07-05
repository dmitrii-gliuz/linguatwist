package org.dmitriigliuz.linguatwist

import org.dmitriigliuz.linguatwist.console.playConsoleGame
import org.dmitriigliuz.linguatwist.core.getResourceBundle
import org.dmitriigliuz.linguatwist.core.resourceBundle

fun main(args: Array<String>) {
    if (args.size > 1) resourceBundle = getResourceBundle(args[1])
    playConsoleGame(args)
}