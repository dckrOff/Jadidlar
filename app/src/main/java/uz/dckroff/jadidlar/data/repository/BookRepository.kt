package uz.dckroff.jadidlar.data.repository

import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.utils.Resource

class BookRepository {
    private val db = FirebaseManager.firestore
    private val collection = db.collection("books")

    suspend fun getTopBooks(limit: Int = 10): Resource<List<Book>> {
        return try {
            val snapshot = collection
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val books = snapshot.toObjects(Book::class.java)
            Resource.Success(books)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch top books")
        }
    }

    suspend fun getAllBooks(): Resource<List<Book>> {
        return try {
            val snapshot = collection
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val books = snapshot.toObjects(Book::class.java)
            Resource.Success(books)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch all books")
        }
    }

    suspend fun getBooksByAuthor(authorId: String): Resource<List<Book>> {
        return try {
            val snapshot = collection
                .whereEqualTo("authorId", authorId)
                .get()
                .await()
            
            val books = snapshot.toObjects(Book::class.java)
            val sortedBooks = books.sortedByDescending { it.publishYear }
            Resource.Success(sortedBooks)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch books by author")
        }
    }

    suspend fun getBookById(bookId: String): Resource<Book> {
        return try {
            val snapshot = collection.document(bookId).get().await()
            val book = snapshot.toObject(Book::class.java)
            if (book != null) {
                Resource.Success(book)
            } else {
                Resource.Error("Book not found")
            }
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch book")
        }
    }

    suspend fun incrementRating(bookId: String): Resource<Boolean> {
        return try {
            val docRef = collection.document(bookId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentRating = snapshot.getLong("rating") ?: 0
                transaction.update(docRef, "rating", currentRating + 1)
            }.await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to increment rating")
        }
    }

    suspend fun searchBooks(query: String): Resource<List<Book>> {
        return try {
            val snapshot = collection.get().await()
            val books = snapshot.toObjects(Book::class.java)
            val filtered = books.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.authorName.contains(query, ignoreCase = true)
            }
            Resource.Success(filtered)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to search books")
        }
    }
}
