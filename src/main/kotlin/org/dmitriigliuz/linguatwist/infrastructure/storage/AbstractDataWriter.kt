package org.dmitriigliuz.linguatwist.infrastructure.storage

abstract class AbstractDataWriter : DataWriter {
    protected abstract val supportedExtensions: Set<String>
    override fun supports(fileExtension: String) = supportedExtensions.contains(fileExtension.lowercase())
}