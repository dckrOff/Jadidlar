package uz.dckroff.jadidlar.utils

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun addToFavorites(itemId: String, type: FavoriteType) {
        val key = getKey(type)
        val favorites = getFavorites(type).toMutableSet()
        favorites.add(itemId)
        prefs.edit().putStringSet(key, favorites).apply()
    }

    fun removeFromFavorites(itemId: String, type: FavoriteType) {
        val key = getKey(type)
        val favorites = getFavorites(type).toMutableSet()
        favorites.remove(itemId)
        prefs.edit().putStringSet(key, favorites).apply()
    }

    fun isFavorite(itemId: String, type: FavoriteType): Boolean {
        return getFavorites(type).contains(itemId)
    }

    fun getFavorites(type: FavoriteType): Set<String> {
        val key = getKey(type)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(itemId: String, type: FavoriteType): Boolean {
        return if (isFavorite(itemId, type)) {
            removeFromFavorites(itemId, type)
            false
        } else {
            addToFavorites(itemId, type)
            true
        }
    }

    private fun getKey(type: FavoriteType): String {
        return when (type) {
            FavoriteType.JADID -> KEY_FAVORITE_JADIDS
            FavoriteType.BOOK -> KEY_FAVORITE_BOOKS
        }
    }

    companion object {
        private const val PREFS_NAME = "jadidlar_prefs"
        private const val KEY_FAVORITE_JADIDS = "favorite_jadids"
        private const val KEY_FAVORITE_BOOKS = "favorite_books"
    }
}

enum class FavoriteType {
    JADID, BOOK
}
