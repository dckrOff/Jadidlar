package uz.dckroff.jadidlar.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.data.repository.TestRepository
import uz.dckroff.jadidlar.utils.Resource

class QuizListViewModel : ViewModel() {
    private val repository = TestRepository()

    private val _tests = MutableLiveData<Resource<List<Test>>>()
    val tests: LiveData<Resource<List<Test>>> = _tests

    init {
        loadTests()
    }

    fun loadTests() {
        _tests.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getAllTests()
            _tests.value = result
        }
    }
}
