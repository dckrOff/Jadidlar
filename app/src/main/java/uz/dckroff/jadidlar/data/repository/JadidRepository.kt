package uz.dckroff.jadidlar.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Jadid
import uz.dckroff.jadidlar.utils.Resource
import java.net.UnknownHostException

class JadidRepository {
    private val db = FirebaseManager.firestore
    private val collection = db.collection("jadids")

    suspend fun getJadids(limit: Int = 10): Resource<List<Jadid>> {
        return try {
            val snapshot = collection
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val jadids = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Jadid::class.java)?.copy(id = doc.id)
            }
            Resource.Success(jadids)
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
            Resource.Error(e.message ?: "Jadidlarni yuklashda xatolik")
        }
    }

    suspend fun getAllJadids(): Resource<List<Jadid>> {
        return try {
            val snapshot = collection
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val jadids = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Jadid::class.java)?.copy(id = doc.id)
            }
            Resource.Success(jadids)
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
            Resource.Error(e.message ?: "Barcha jadidlarni yuklashda xatolik")
        }
    }

    suspend fun getJadidById(jadidId: String): Resource<Jadid> {
        // Валидация: проверяем что ID не пустой и не является названием коллекции
        if (jadidId.isBlank() || jadidId == "jadids") {
            return Resource.Error("Noto'g'ri jadid ID")
        }
        
        return try {
            val snapshot = collection.document(jadidId).get().await()
            val jadid = snapshot.toObject(Jadid::class.java)?.copy(id = snapshot.id)
            if (jadid != null) {
                Resource.Success(jadid)
            } else {
                Resource.Error("Jadid topilmadi")
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
            Resource.Error(e.message ?: "Jadidni yuklashda xatolik")
        }
    }
}
