package uz.dckroff.jadidlar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.data.models.Jadid
import uz.dckroff.jadidlar.data.repository.BookRepository
import uz.dckroff.jadidlar.data.repository.JadidRepository
import uz.dckroff.jadidlar.utils.Resource

class HomeViewModel : ViewModel() {
    private val jadidRepository = JadidRepository()
    private val bookRepository = BookRepository()

    private val _jadids = MutableLiveData<Resource<List<Jadid>>>()
    val jadids: LiveData<Resource<List<Jadid>>> = _jadids

    private val _topBooks = MutableLiveData<Resource<List<Book>>>()
    val topBooks: LiveData<Resource<List<Book>>> = _topBooks

    init {
        loadData()
    }

    fun loadData() {
        loadJadids()
        loadTopBooks()
    }

    private fun loadJadids() {
        _jadids.value = Resource.Loading
        viewModelScope.launch {
            val result = jadidRepository.getJadids(10)
            _jadids.value = result
        }
    }

    private fun loadTopBooks() {
        _topBooks.value = Resource.Loading
        viewModelScope.launch {
            val result = bookRepository.getTopBooks(10)
            _topBooks.value = result
        }
    }
}
