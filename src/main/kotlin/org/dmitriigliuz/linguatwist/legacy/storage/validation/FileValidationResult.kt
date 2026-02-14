package org.dmitriigliuz.linguatwist.legacy.storage.validation

data class FileValidationResult(
    val isValid: Boolean,
    val issues: List<String>
){
    fun getErrorMessages(): String = issues.joinToString("; ")

}