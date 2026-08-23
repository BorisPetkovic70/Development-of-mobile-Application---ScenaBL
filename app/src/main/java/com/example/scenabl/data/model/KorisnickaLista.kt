package com.example.scenabl.data.model

import com.google.firebase.firestore.DocumentId

data class KorisnickaLista(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val titleId: String = "",
    val tipListe: String = ListType.ZELIM_GLEDATI
)

object ListType {
    const val ZELIM_GLEDATI = "zelim_gledati"
    const val ODGLEDANO = "odgledano"
}
