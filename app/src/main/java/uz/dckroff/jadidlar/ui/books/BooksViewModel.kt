package uz.dckroff.jadidlar.ui.books

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.data.repository.BookRepository
import uz.dckroff.jadidlar.utils.Resource

class BooksViewModel : ViewModel() {
    private val repository = BookRepository()

    private val _books = MutableLiveData<Resource<List<Book>>>()
    val books: LiveData<Resource<List<Book>>> = _books

    private var allBooks: List<Book> = emptyList()

    init {
        loadBooks()
    }

    fun loadBooks() {
        _books.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getAllBooks()
            _books.value = result
            if (result is Resource.Success) {
                allBooks = result.data
            }
        }
    }

    fun searchBooks(query: String) {
        if (query.isEmpty()) {
            _books.value = Resource.Success(allBooks)
            return
        }
        
        viewModelScope.launch {
            val result = repository.searchBooks(query)
            _books.value = result
        }
    }

    fun sortByRating() {
        val sorted = allBooks.sortedByDescending { it.rating }
        _books.value = Resource.Success(sorted)
    }

    fun sortByYear() {
        val sorted = allBooks.sortedByDescending { it.publishYear }
        _books.value = Resource.Success(sorted)
    }

    fun showAll() {
        _books.value = Resource.Success(allBooks)
    }
}
