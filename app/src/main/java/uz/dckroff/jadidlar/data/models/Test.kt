package uz.dckroff.jadidlar.data.models

import com.google.firebase.firestore.DocumentId

data class Test(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questionCount: Int = 0,
    val timeLimit: Int = 0,
    val questions: List<Question> = emptyList()
)
