package uz.dckroff.jadidlar.data.models

data class Question(
    val id: String = "",
    val questionText: String = "",
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
)
