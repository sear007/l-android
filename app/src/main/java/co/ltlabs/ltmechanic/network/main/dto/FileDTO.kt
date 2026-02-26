package co.ltlabs.ltmechanic.network.main.dto

import co.ltlabs.ltmechanic.domain.FileResult as FileResultDomain
import co.ltlabs.ltmechanic.domain.Language as LanguageDomain

data class FileResult(
    val fieldname: String,
    val originalname: String,
    val encoding: String,
    val mimetype: String,
    val destination: String,
    val filename: String,
    val path: String,
    val size: Int
)

data class Language (
    val code: String,
    val language: String,
    val country: String,
    val factory: String
)

fun List<Language>.asLanguageDomainModel(): List<LanguageDomain> {
    return map {
        LanguageDomain (
            it.code,
            it.language,
            it.country,
            it.factory
        )
    }
}

fun List<FileResult>.asFileResultDomainModel(): List<FileResultDomain> {
    return map {
        FileResultDomain (
            it.path
        )
    }
}