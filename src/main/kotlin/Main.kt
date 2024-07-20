package org.dmitriigliuz.linguatwist

import org.dmitriigliuz.linguatwist.console.main
import org.dmitriigliuz.linguatwist.core.getResourceBundle
import org.dmitriigliuz.linguatwist.core.resourceBundle

enum class Arguments(private val long: String, private val short: String, private val defaultValue: String) {
    VOCABULARY("--vocabulary", "-v", "vocabulary.xlsx"),
    LANGUAGE("--language", "-l", "en");

    fun getValue(args: Array<String>): String {
        val argIndex = args.indexOf(long)

        return if (argIndex != -1) {
            args[argIndex + 1]
        } else {
            val shortArgIndex = args.indexOf(short)
            if (shortArgIndex != -1) {
                args[shortArgIndex + 1]
            } else {
                defaultValue
            }
        }
    }
}

lateinit var vocabularyPath: String

fun main(args: Array<String>) {
    vocabularyPath = Arguments.VOCABULARY.getValue(args)
    val languageTag = Arguments.LANGUAGE.getValue(args)
    resourceBundle = getResourceBundle(languageTag)
    main()
}