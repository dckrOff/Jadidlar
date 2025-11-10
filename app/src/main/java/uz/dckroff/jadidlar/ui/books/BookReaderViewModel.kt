package uz.dckroff.jadidlar.ui.books

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uz.dckroff.jadidlar.utils.ReadingProgressManager

class BookReaderViewModel : ViewModel() {
    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage

    private lateinit var progressManager: ReadingProgressManager

    fun initialize(context: Context, bookId: String) {
        progressManager = ReadingProgressManager(context)
        val lastPage = progressManager.loadLastPage(bookId)
        _currentPage.value = lastPage
    }

    fun saveCurrentPage(bookId: String, page: Int) {
        progressManager.saveCurrentPage(bookId, page)
        _currentPage.value = page
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }
}
