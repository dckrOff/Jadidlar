package uz.dckroff.jadidlar.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseManager {
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    fun getAnalytics(context: android.content.Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
    
    val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }
}
