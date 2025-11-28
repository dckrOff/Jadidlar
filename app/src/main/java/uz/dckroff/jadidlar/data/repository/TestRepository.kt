package uz.dckroff.jadidlar.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import uz.dckroff.jadidlar.data.models.Test
import uz.dckroff.jadidlar.data.models.TestResult
import uz.dckroff.jadidlar.utils.Resource
import java.net.UnknownHostException

class TestRepository {
    private val db = FirebaseManager.firestore
    private val testsCollection = db.collection("tests")
    private val maxsusTestsCollection = db.collection("maxsus_tests")
    private val resultsCollection = db.collection("test_results")

    suspend fun getAllTests(): Resource<List<Test>> {
        return try {
            val snapshot = testsCollection.get().await()
            val tests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Test::class.java)?.copy(id = doc.id)
            }
            Resource.Success(tests)
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
            Resource.Error(e.message ?: "Testlarni yuklashda xatolik")
        }
    }

    suspend fun getTestById(testId: String): Resource<Test> {
        // Валидация: проверяем что ID не пустой и не является названием коллекции
        if (testId.isBlank() || testId == "tests" || testId == "maxsus_tests") {
            return Resource.Error("Noto'g'ri test ID")
        }
        
        return try {
            // Сначала пытаемся загрузить из коллекции tests
            val snapshot = testsCollection.document(testId).get().await()
            val test = snapshot.toObject(Test::class.java)?.copy(id = snapshot.id)
            if (test != null) {
                return Resource.Success(test)
            }
            
            // Если не найден в tests, пытаемся загрузить из maxsus_tests
            val maxsusSnapshot = maxsusTestsCollection.document(testId).get().await()
            val maxsusTest = maxsusSnapshot.toObject(Test::class.java)?.copy(id = maxsusSnapshot.id)
            if (maxsusTest != null) {
                return Resource.Success(maxsusTest)
            }
            
            Resource.Error("Test topilmadi")
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
            Resource.Error(e.message ?: "Testni yuklashda xatolik")
        }
    }

    /**
     * Получает вводный тест (onboarding quiz)
     * @return Resource с вводным тестом или null если не найден
     */
    suspend fun getOnboardingQuiz(): Resource<Test?> {
        return try {
            val snapshot = testsCollection
                .whereEqualTo("isOnboardingQuiz", true)
                .limit(1)
                .get()
                .await()
            
            val test = snapshot.documents.firstOrNull()?.toObject(Test::class.java)?.copy(
                id = snapshot.documents.firstOrNull()?.id ?: ""
            )
            
            Resource.Success(test)
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
            Resource.Error(e.message ?: "Kirish testini yuklashda xatolik")
        }
    }

    /**
     * Получает все специальные тесты из коллекции maxsus_tests
     * Исключает тесты с isOnboardingQuiz = true
     * @return Resource со списком специальных тестов
     */
    suspend fun getAllMaxsusTests(): Resource<List<Test>> {
        return try {
            val snapshot = maxsusTestsCollection.get().await()
            val tests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Test::class.java)?.copy(id = doc.id)
            }.filter { test ->
                // Исключаем тесты с isOnboardingQuiz = true
                !test.isOnboardingQuiz
            }
            Resource.Success(tests)
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
            Resource.Error(e.message ?: "Maxsus testlarni yuklashda xatolik")
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
            Resource.Error(e.message ?: "Test natijasini saqlashda xatolik")
        }
    }
}
