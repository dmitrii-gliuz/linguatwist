package org.dmitriigliuz.linguatwist.legacy.storage

abstract class AbstractDataLoader : org.dmitriigliuz.linguatwist.legacy.storage.DataLoader {
    protected abstract val supportedExttensions: Set<String>
    override fun supports(fileExtension: String) = supportedExttensions.contains(fileExtension.lowercase())
}