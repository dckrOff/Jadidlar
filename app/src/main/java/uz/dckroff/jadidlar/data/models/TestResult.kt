package uz.dckroff.jadidlar.data.models

import com.google.firebase.Timestamp

data class TestResult(
    val id: String = "",
    val testId: String = "",
    val userId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val timeSpent: Long = 0,
    val completedAt: Timestamp? = null,
    val answers: List<Answer> = emptyList()
)

data class Answer(
    val questionId: String = "",
    val selectedAnswerIndex: Int = -1,
    val isCorrect: Boolean = false
)
