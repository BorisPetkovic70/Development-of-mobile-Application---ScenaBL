package com.example.scenabl.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Recenzija(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val titleId: String = "",
    val ocjena: Int = 0,
    val komentar: String = "",
    val datum: Timestamp = Timestamp.now()
)
