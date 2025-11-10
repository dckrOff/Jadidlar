package uz.dckroff.jadidlar.ui.books

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.data.repository.BookRepository
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.DownloadUtils
import uz.dckroff.jadidlar.utils.FavoriteType
import uz.dckroff.jadidlar.utils.FavoritesManager
import uz.dckroff.jadidlar.utils.Resource

class BookDetailViewModel : ViewModel() {
    private val repository = BookRepository()

    private val _book = MutableLiveData<Resource<Book>>()
    val book: LiveData<Resource<Book>> = _book

    private val _otherBooks = MutableLiveData<Resource<List<Book>>>()
    val otherBooks: LiveData<Resource<List<Book>>> = _otherBooks

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    fun loadBook(bookId: String, context: Context) {
        _book.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getBookById(bookId)
            _book.value = result
            
            if (result is Resource.Success) {
                val book = result.data
                loadOtherBooks(book.authorId, bookId)
                repository.incrementRating(bookId)
                AnalyticsHelper.logBookOpened(context, book.id, book.title, book.authorId)
                
                val favoritesManager = FavoritesManager(context)
                _isFavorite.value = favoritesManager.isFavorite(bookId, FavoriteType.BOOK)
            }
        }
    }

    private fun loadOtherBooks(authorId: String, excludeBookId: String) {
        viewModelScope.launch {
            val result = repository.getBooksByAuthor(authorId)
            if (result is Resource.Success) {
                val filtered = result.data.filter { it.id != excludeBookId }
                _otherBooks.value = Resource.Success(filtered)
            } else {
                _otherBooks.value = result
            }
        }
    }

    fun toggleFavorite(bookId: String, context: Context) {
        val favoritesManager = FavoritesManager(context)
        val newState = favoritesManager.toggleFavorite(bookId, FavoriteType.BOOK)
        _isFavorite.value = newState
        
        if (newState) {
            AnalyticsHelper.logAddedToFavorites(context, "book", bookId)
        }
    }

    fun downloadBook(context: Context, url: String, fileName: String) {
        viewModelScope.launch {
            val book = (_book.value as? Resource.Success)?.data ?: return@launch
            DownloadUtils.downloadPdf(context, url, fileName)
            AnalyticsHelper.logBookDownloaded(context, book.id)
        }
    }
}
