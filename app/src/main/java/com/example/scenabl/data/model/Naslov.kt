package com.example.scenabl.data.model

import com.google.firebase.firestore.DocumentId

data class Naslov(
    @DocumentId
    val id: String = "",
    val naziv: String = "",
    val opis: String = "",
    val reziser: String = "",
    val trajanje: Int = 0,
    val zanr: String = "",
    val tip: String = TitleType.POZORISTE,
    val slikaUrl: String = "",
    val institutionId: String = ""
)

object TitleType {
    const val POZORISTE = "pozoriste"
    const val BIOSKOP = "bioskop"
}
