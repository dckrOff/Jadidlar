package uz.dckroff.jadidlar.utils

import android.content.Context
import android.content.SharedPreferences

class ReadingProgressManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCurrentPage(bookId: String, page: Int) {
        prefs.edit().putInt(getKey(bookId), page).apply()
    }

    fun loadLastPage(bookId: String): Int {
        return prefs.getInt(getKey(bookId), 0)
    }

    private fun getKey(bookId: String): String {
        return "book_page_$bookId"
    }

    companion object {
        private const val PREFS_NAME = "reading_progress"
    }
}
