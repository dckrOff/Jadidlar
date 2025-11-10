package uz.dckroff.jadidlar.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import uz.dckroff.jadidlar.data.firebase.FirebaseManager

object AnalyticsHelper {
    
    fun logBookOpened(context: Context, bookId: String, bookTitle: String, authorId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("book_id", bookId)
            putString("book_title", bookTitle)
            putString("author_id", authorId)
        }
        analytics.logEvent("book_opened", bundle)
    }

    fun logReadingStarted(context: Context, bookId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("book_id", bookId)
        }
        analytics.logEvent("reading_started", bundle)
    }

    fun logTestStarted(context: Context, testId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("test_id", testId)
        }
        analytics.logEvent("test_started", bundle)
    }

    fun logTestCompleted(context: Context, testId: String, score: Int, timeSpent: Long) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("test_id", testId)
            putInt("score", score)
            putLong("time_spent", timeSpent)
        }
        analytics.logEvent("test_completed", bundle)
    }

    fun logSearchPerformed(context: Context, searchQuery: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("search_query", searchQuery)
        }
        analytics.logEvent("search_performed", bundle)
    }

    fun logAddedToFavorites(context: Context, itemType: String, itemId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("item_type", itemType)
            putString("item_id", itemId)
        }
        analytics.logEvent("added_to_favorites", bundle)
    }

    fun logBookDownloaded(context: Context, bookId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("book_id", bookId)
        }
        analytics.logEvent("book_downloaded", bundle)
    }

    fun logJadidViewed(context: Context, jadidId: String) {
        val analytics = FirebaseManager.getAnalytics(context)
        val bundle = Bundle().apply {
            putString("jadid_id", jadidId)
        }
        analytics.logEvent("jadid_viewed", bundle)
    }
}
