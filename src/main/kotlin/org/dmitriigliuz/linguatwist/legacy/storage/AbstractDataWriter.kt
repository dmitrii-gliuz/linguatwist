package org.dmitriigliuz.linguatwist.legacy.storage

abstract class AbstractDataWriter : org.dmitriigliuz.linguatwist.legacy.storage.DataWriter {
    protected abstract val supportedExtensions: Set<String>
    override fun supports(fileExtension: String) = supportedExtensions.contains(fileExtension.lowercase())
}