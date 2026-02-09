package org.dmitriigliuz.linguatwist.infrastructure.config

import org.dmitriigliuz.linguatwist.AppConfig

data class ConfigLoadResult(
    val config: AppConfig,
    val source: ConfigSource,
    val warnings: List<String> = emptyList(),
    val loadedFrom: String? = null
) {
    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()

    fun getWarningsMessage() = warnings.joinToString("\n")
}
