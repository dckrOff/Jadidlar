package uz.dckroff.jadidlar.data.repository

import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Jadid
import uz.dckroff.jadidlar.utils.Resource

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
            
            val jadids = snapshot.toObjects(Jadid::class.java)
            Resource.Success(jadids)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch jadids")
        }
    }

    suspend fun getAllJadids(): Resource<List<Jadid>> {
        return try {
            val snapshot = collection
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val jadids = snapshot.toObjects(Jadid::class.java)
            Resource.Success(jadids)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch all jadids")
        }
    }

    suspend fun getJadidById(jadidId: String): Resource<Jadid> {
        return try {
            val snapshot = collection.document(jadidId).get().await()
            val jadid = snapshot.toObject(Jadid::class.java)
            if (jadid != null) {
                Resource.Success(jadid)
            } else {
                Resource.Error("Jadid not found")
            }
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch jadid")
        }
    }
}
