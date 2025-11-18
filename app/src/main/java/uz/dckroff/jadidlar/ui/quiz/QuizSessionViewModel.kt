package uz.dckroff.jadidlar.ui.quiz

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Question
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.data.repository.TestRepository
import uz.dckroff.jadidlar.utils.Resource

class QuizSessionViewModel : ViewModel() {
    private val repository = TestRepository()

    private val _test = MutableLiveData<Resource<Test>>()
    val test: LiveData<Resource<Test>> = _test

    private val _currentQuestionIndex = MutableLiveData(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val userAnswers = mutableMapOf<Int, Int>()
    private var timer: CountDownTimer? = null
    private var startTime: Long = 0

    fun loadTest(testId: String) {
        _test.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getTestById(testId)
            _test.value = result
            
            if (result is Resource.Success) {
                startTime = System.currentTimeMillis()
                startTimer(result.data.timeLimit * 60 * 1000L)
                updateCurrentQuestion()
            }
        }
    }

    private fun startTimer(durationMillis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeRemaining.value = 0
            }
        }.start()
    }

    fun nextQuestion() {
        val currentTest = (_test.value as? Resource.Success)?.data ?: return
        val nextIndex = (_currentQuestionIndex.value ?: 0) + 1
        if (nextIndex < currentTest.questions.size) {
            _currentQuestionIndex.value = nextIndex
            updateCurrentQuestion()
        }
    }

    fun previousQuestion() {
        val prevIndex = (_currentQuestionIndex.value ?: 0) - 1
        if (prevIndex >= 0) {
            _currentQuestionIndex.value = prevIndex
            updateCurrentQuestion()
        }
    }

    fun saveAnswer(answerIndex: Int) {
        val currentIndex = _currentQuestionIndex.value ?: 0
        userAnswers[currentIndex] = answerIndex
    }

    fun getSelectedAnswer(): Int? {
        val currentIndex = _currentQuestionIndex.value ?: 0
        return userAnswers[currentIndex]
    }

    private fun updateCurrentQuestion() {
        val currentTest = (_test.value as? Resource.Success)?.data ?: return
        val currentIndex = _currentQuestionIndex.value ?: 0
        if (currentIndex < currentTest.questions.size) {
            _currentQuestion.value = currentTest.questions[currentIndex]
        }
    }

    fun calculateResult(): Triple<Int, Int, Long> {
        val currentTest = (_test.value as? Resource.Success)?.data ?: return Triple(0, 0, 0)
        var correctCount = 0
        
        currentTest.questions.forEachIndexed { index, question ->
            val userAnswer = userAnswers[index] ?: -1
            if (userAnswer == question.correctAnswerIndex) {
                correctCount++
            }
        }
        
        val timeSpent = System.currentTimeMillis() - startTime
        return Triple(correctCount, currentTest.questions.size, timeSpent)
    }

    fun getUserAnswers(): Map<Int, Int> = userAnswers.toMap()

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
