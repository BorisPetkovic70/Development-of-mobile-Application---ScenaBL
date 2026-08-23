package com.example.scenabl.data.model

import com.google.firebase.firestore.DocumentId

data class Institucija(
    @DocumentId
    val id: String = "",
    val naziv: String = "",
    val opis: String = "",
    val ownerUid: String = ""
)
