package uz.dckroff.jadidlar.data.models

import com.google.firebase.firestore.DocumentId

data class Book(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val publishYear: Int = 0,
    val description: String = "",
    val coverImageUrl: String = "",
    val pdfUrl: String = "",
    val rating: Int = 0,
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0
)
