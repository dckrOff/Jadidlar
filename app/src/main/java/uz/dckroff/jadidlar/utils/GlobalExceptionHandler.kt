package uz.dckroff.jadidlar.utils

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import kotlin.system.exitProcess

/**
 * Глобальный обработчик необработанных исключений
 */
class GlobalExceptionHandler private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            
            // Логируем в Firebase Crashlytics
            FirebaseManager.crashlytics.recordException(throwable)
            FirebaseManager.crashlytics.log("Uncaught exception: ${throwable.message}")
            
            // Даем время для отправки в Crashlytics
            Thread.sleep(1000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in exception handler", e)
        } finally {
            // Вызываем стандартный обработчик
            defaultHandler?.uncaughtException(thread, throwable)
            
            // Завершаем процесс
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        
        /**
         * Установить глобальный обработчик исключений
         */
        fun install(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            val exceptionHandler = GlobalExceptionHandler(context, defaultHandler)
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)
            Log.d(TAG, "Global exception handler installed")
        }
    }
}

