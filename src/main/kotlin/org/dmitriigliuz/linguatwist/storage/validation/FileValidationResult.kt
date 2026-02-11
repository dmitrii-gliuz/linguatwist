package org.dmitriigliuz.linguatwist.storage.validation

data class FileValidationResult(
    val isValid: Boolean,
    val issues: List<String>
){
    fun getErrorMessages(): String = issues.joinToString("; ")

}