package uz.dckroff.jadidlar.data.models

data class Test(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questionCount: Int = 0,
    val timeLimit: Int = 0,
    val questions: List<Question> = emptyList()
)
