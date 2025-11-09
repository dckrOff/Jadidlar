package uz.dckroff.jadidlar.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Answer
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.data.models.TestResult
import uz.dckroff.jadidlar.data.repository.TestRepository
import uz.dckroff.jadidlar.ui.adapters.QuestionResult
import uz.dckroff.jadidlar.utils.Resource

class QuizResultsViewModel : ViewModel() {
    private val repository = TestRepository()

    private val _test = MutableLiveData<Resource<Test>>()
    val test: LiveData<Resource<Test>> = _test

    private val _questionResults = MutableLiveData<List<QuestionResult>>()
    val questionResults: LiveData<List<QuestionResult>> = _questionResults

    fun loadTestResults(testId: String, userAnswers: Map<Int, Int>) {
        _test.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getTestById(testId)
            _test.value = result
            
            if (result is Resource.Success) {
                val test = result.data
                val results = test.questions.mapIndexed { index, question ->
                    val userAnswerIndex = userAnswers[index] ?: -1
                    val isCorrect = userAnswerIndex == question.correctAnswerIndex
                    
                    QuestionResult(
                        questionNumber = index + 1,
                        questionText = question.questionText,
                        correctAnswer = question.answers.getOrNull(question.correctAnswerIndex) ?: "",
                        userAnswer = question.answers.getOrNull(userAnswerIndex) ?: "Javob berilmagan",
                        isCorrect = isCorrect
                    )
                }
                _questionResults.value = results
            }
        }
    }

    fun saveResult(testId: String, score: Int, totalQuestions: Int, timeSpent: Long, userAnswers: Map<Int, Int>) {
        viewModelScope.launch {
            val test = (_test.value as? Resource.Success)?.data ?: return@launch
            
            val answers = test.questions.mapIndexed { index, question ->
                val userAnswerIndex = userAnswers[index] ?: -1
                Answer(
                    questionId = question.id,
                    selectedAnswerIndex = userAnswerIndex,
                    isCorrect = userAnswerIndex == question.correctAnswerIndex
                )
            }
            
            val testResult = TestResult(
                testId = testId,
                userId = "anonymous",
                score = score,
                totalQuestions = totalQuestions,
                timeSpent = timeSpent,
                answers = answers
            )
            
            repository.saveTestResult(testResult)
        }
    }
}
