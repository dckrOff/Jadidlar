package uz.dckroff.jadidlar

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import uz.dckroff.jadidlar.utils.GlobalExceptionHandler

class JadidlarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Инициализация Firebase
        FirebaseApp.initializeApp(this)
        
        // Настройки Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
        
        // Установка глобального обработчика исключений
        GlobalExceptionHandler.install(this)
    }
}
