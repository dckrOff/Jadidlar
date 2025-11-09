package uz.dckroff.jadidlar.data.repository

import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.data.models.TestResult
import uz.dckroff.jadidlar.utils.Resource

class TestRepository {
    private val db = FirebaseManager.firestore
    private val testsCollection = db.collection("tests")
    private val resultsCollection = db.collection("test_results")

    suspend fun getAllTests(): Resource<List<Test>> {
        return try {
            val snapshot = testsCollection.get().await()
            val tests = snapshot.toObjects(Test::class.java)
            Resource.Success(tests)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch tests")
        }
    }

    suspend fun getTestById(testId: String): Resource<Test> {
        return try {
            val snapshot = testsCollection.document(testId).get().await()
            val test = snapshot.toObject(Test::class.java)
            if (test != null) {
                Resource.Success(test)
            } else {
                Resource.Error("Test not found")
            }
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to fetch test")
        }
    }

    suspend fun saveTestResult(testResult: TestResult): Resource<String> {
        return try {
            val docRef = resultsCollection.document()
            val resultWithId = testResult.copy(
                id = docRef.id,
                completedAt = Timestamp.now()
            )
            docRef.set(resultWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            FirebaseManager.crashlytics.recordException(e)
            Resource.Error(e.message ?: "Failed to save test result")
        }
    }
}
