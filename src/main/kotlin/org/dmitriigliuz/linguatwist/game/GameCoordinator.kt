package org.dmitriigliuz.linguatwist.game

import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter

class GameCoordinator(
    private val config: GameConfig,
    private val loader: VocabularyLoader,
    private val writer: VocabularyWriter
) {
    val engine = GameEngine(config)

    fun defaultNumberOfRounds(vocabulary: Vocabulary, config: GameConfig) =
        if (vocabulary.size < config.maxRounds * config.pairsPerRound) {
            val leftoverPairs = vocabulary.size % config.pairsPerRound
            vocabulary.size / config.pairsPerRound + if (leftoverPairs > 0) 1 else 0
        } else config.defaultRounds

    fun loadVocabulary(): Result<VocabularyLoadResult> {

    }

    fun startNewGame() {
    }
}