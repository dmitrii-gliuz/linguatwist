package org.dmitriigliuz.linguatwist.storage

abstract class AbstractDataWriter : org.dmitriigliuz.linguatwist.storage.DataWriter {
    protected abstract val supportedExtensions: Set<String>
    override fun supports(fileExtension: String) = supportedExtensions.contains(fileExtension.lowercase())
}