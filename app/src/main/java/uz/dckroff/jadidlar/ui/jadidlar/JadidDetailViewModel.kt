package uz.dckroff.jadidlar.ui.jadidlar

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

class JadidDetailViewModel : ViewModel() {
    private val jadidRepository = JadidRepository()
    private val bookRepository = BookRepository()

    private val _jadid = MutableLiveData<Resource<Jadid>>()
    val jadid: LiveData<Resource<Jadid>> = _jadid

    private val _books = MutableLiveData<Resource<List<Book>>>()
    val books: LiveData<Resource<List<Book>>> = _books

    private val _isDescriptionExpanded = MutableLiveData(false)
    val isDescriptionExpanded: LiveData<Boolean> = _isDescriptionExpanded

    fun loadJadid(jadidId: String) {
        _jadid.value = Resource.Loading
        viewModelScope.launch {
            val result = jadidRepository.getJadidById(jadidId)
            _jadid.value = result
            
            if (result is Resource.Success) {
                loadBooks(result.data.id)
            }
        }
    }

    private fun loadBooks(authorId: String) {
        _books.value = Resource.Loading
        viewModelScope.launch {
            val result = bookRepository.getBooksByAuthor(authorId)
            _books.value = result
        }
    }

    fun toggleDescription() {
        _isDescriptionExpanded.value = !(_isDescriptionExpanded.value ?: false)
    }
}
