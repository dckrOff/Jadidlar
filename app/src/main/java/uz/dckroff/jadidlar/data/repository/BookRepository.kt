package uz.dckroff.jadidlar.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Book
import uz.dckroff.jadidlar.utils.Resource
import java.net.UnknownHostException

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
            
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            Resource.Success(books)
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Top kitoblarni yuklashda xatolik")
        }
    }

    suspend fun getAllBooks(): Resource<List<Book>> {
        return try {
            val snapshot = collection
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            Resource.Success(books)
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Barcha kitoblarni yuklashda xatolik")
        }
    }

    suspend fun getBooksByAuthor(authorId: String): Resource<List<Book>> {
        return try {
            val snapshot = collection
                .whereEqualTo("authorId", authorId)
                .get()
                .await()
            
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            val sortedBooks = books.sortedByDescending { it.publishYear }
            Resource.Success(sortedBooks)
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Muallif kitoblarini yuklashda xatolik")
        }
    }

    suspend fun getBookById(bookId: String): Resource<Book> {
        // Валидация: проверяем что ID не пустой и не является названием коллекции
        if (bookId.isBlank() || bookId == "books") {
            return Resource.Error("Noto'g'ri kitob ID")
        }
        
        return try {
            val snapshot = collection.document(bookId).get().await()
            val book = snapshot.toObject(Book::class.java)?.copy(id = snapshot.id)
            if (book != null) {
                Resource.Success(book)
            } else {
                Resource.Error("Kitob topilmadi")
            }
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Kitobni yuklashda xatolik")
        }
    }

    suspend fun incrementRating(bookId: String): Resource<Boolean> {
        // Валидация: проверяем что ID не пустой и не является названием коллекции
        if (bookId.isBlank() || bookId == "books") {
            return Resource.Error("Noto'g'ri kitob ID")
        }
        
        return try {
            val docRef = collection.document(bookId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentRating = snapshot.getLong("rating") ?: 0
                transaction.update(docRef, "rating", currentRating + 1)
            }.await()
            Resource.Success(true)
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Reytingni o'zgartirishda xatolik")
        }
    }

    suspend fun searchBooks(query: String): Resource<List<Book>> {
        return try {
            val snapshot = collection.get().await()
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Book::class.java)?.copy(id = doc.id)
            }
            val filtered = books.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.authorName.contains(query, ignoreCase = true)
            }
            Resource.Success(filtered)
        } catch (e: FirebaseNetworkException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet bilan bog'lanishda xatolik")
        } catch (e: UnknownHostException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Internet mavjud emas")
        } catch (e: FirebaseFirestoreException) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error("Ma'lumotlar bazasiga ulanishda xatolik")
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Kitoblarni qidirishda xatolik")
        }
    }
}
