package org.dmitriigliuz.linguatwist.infrastructure.storage

abstract class AbstractDataLoader : DataLoader {
    protected abstract val supportedExttensions: Set<String>
    override fun supports(fileExtension: String) = supportedExttensions.contains(fileExtension.lowercase())
}