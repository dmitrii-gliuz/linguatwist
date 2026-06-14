package org.dmitriigliuz.linguatwist.console

sealed class Feedback {
    abstract val message: String
    data class Initial(override val message: String) : Feedback()
    data class Correct(override val message: String) : Feedback()
    data class Wrong(override val message: String) : Feedback()
    data class Invalid(override val message: String) : Feedback()
    data class Finished(override val message: String): Feedback()
}
