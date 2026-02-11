package org.dmitriigliuz.linguatwist.storage

abstract class AbstractDataLoader : org.dmitriigliuz.linguatwist.storage.DataLoader {
    protected abstract val supportedExttensions: Set<String>
    override fun supports(fileExtension: String) = supportedExttensions.contains(fileExtension.lowercase())
}