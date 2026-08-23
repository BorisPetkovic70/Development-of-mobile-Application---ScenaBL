package com.example.scenabl.data.model

import com.google.firebase.firestore.DocumentId

data class Korisnik(
    @DocumentId
    val uid: String = "",
    val ime: String = "",
    val prezime: String = "",
    val email: String = "",
    val role: String = UserRole.VIEWER,
    val favoriteGenres: List<String> = emptyList(),
    val profileImageUrl: String? = null
)

object UserRole {
    const val VIEWER = "viewer"
    const val ORGANIZER = "organizer"
}

object Genres {
    const val COMEDY = "Komedija"
    const val DRAMA = "Drama"
    const val THRILLER = "Triler"
    const val MUSICAL = "Muzička predstava"
    const val DOCUMENTARY = "Dokumentarni film"
    const val ACTION = "Akcija"
    const val ANIMATED = "Animirani film"
    const val HORROR = "Horor"

    val ALL = listOf(COMEDY, DRAMA, THRILLER, MUSICAL, DOCUMENTARY, ACTION, ANIMATED, HORROR)
}
