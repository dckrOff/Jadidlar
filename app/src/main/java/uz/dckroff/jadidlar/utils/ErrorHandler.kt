package uz.dckroff.jadidlar.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.firebase.FirebaseManager
import java.net.UnknownHostException

object ErrorHandler {
    const val TAG = "ErrorHandler"

    /**
     * Показать диалог с ошибкой
     */
    fun showErrorDialog(
        context: Context,
        title: String = "Xatolik",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        try {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    onDismiss?.invoke()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog: ${e.message}", e)
        }
    }

    /**
     * Обработать ошибку Resource и показать диалог
     */
    fun handleResourceError(
        fragment: Fragment,
        error: Resource.Error,
        onDismiss: (() -> Unit)? = null
    ) {
        val context = fragment.context ?: return
        val userFriendlyMessage = getUserFriendlyMessage(error.message)

        // Логируем в Crashlytics
        FirebaseManager.crashlytics.log("Resource Error: ${error.message}")

        showErrorDialog(context, message = userFriendlyMessage, onDismiss = onDismiss)
    }

    /**
     * Обработать исключение и показать диалог
     */
    fun handleException(
        context: Context,
        exception: Throwable,
        customMessage: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        Log.e(TAG, "Exception occurred: ${exception.message}", exception)

        // Логируем в Crashlytics
        FirebaseManager.crashlytics.recordException(exception)
        FirebaseManager.crashlytics.log("Exception: ${exception.javaClass.simpleName} - ${exception.message}")

        val userFriendlyMessage = customMessage ?: getUserFriendlyMessage(exception)
        showErrorDialog(context, message = userFriendlyMessage, onDismiss = onDismiss)
    }

    /**
     * Получить понятное пользователю сообщение об ошибке
     */
    private fun getUserFriendlyMessage(error: String): String {
        return when {
            error.contains("network", ignoreCase = true) ||
                    error.contains("internet", ignoreCase = true) ||
                    error.contains("connection", ignoreCase = true) ->
                "Internet bilan bog'lanishda xatolik. Iltimos, internetni tekshiring va qayta urinib ko'ring."

            error.contains("not found", ignoreCase = true) ->
                "Ma'lumot topilmadi."

            error.contains("invalid", ignoreCase = true) ->
                "Noto'g'ri ma'lumot."

            error.contains("permission", ignoreCase = true) ->
                "Ruxsat berilmagan."

            error.contains("timeout", ignoreCase = true) ->
                "Vaqt tugadi. Iltimos, qayta urinib ko'ring."

            else -> "Xatolik yuz berdi: $error"
        }
    }

    /**
     * Получить понятное пользователю сообщение из исключения
     */
    private fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is FirebaseNetworkException,
            is UnknownHostException ->
                "Internet bilan bog'lanishda xatolik. Iltimos, internetni tekshiring."

            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        "Server bilan bog'lanishda xatolik. Iltimos, keyinroq urinib ko'ring."

                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        "Ruxsat berilmagan."

                    FirebaseFirestoreException.Code.NOT_FOUND ->
                        "Ma'lumot topilmadi."

                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                        "Vaqt tugadi. Iltimos, qayta urinib ko'ring."

                    else ->
                        "Ma'lumotlarni yuklashda xatolik: ${exception.message ?: "Noma'lum xatolik"}"
                }
            }

            is FirebaseException ->
                "Firebase xatolik: ${exception.message ?: "Noma'lum xatolik"}"

            is NullPointerException ->
                "Ma'lumot topilmadi yoki noto'g'ri."

            is IllegalStateException ->
                "Dastur noto'g'ri holatda."

            is IllegalArgumentException ->
                "Noto'g'ri ma'lumot kiritildi."

            else ->
                "Xatolik yuz berdi: ${exception.message ?: "Noma'lum xatolik"}"
        }
    }

    /**
     * Обработать ошибку с возможностью повтора
     */
    fun showErrorWithRetry(
        context: Context,
        message: String,
        onRetry: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        try {
            Log.e(TAG, "showErrorWithRetry: " + message)
            AlertDialog.Builder(context)
                .setTitle("Xatolik")
                .setMessage(message)
                .setPositiveButton("Qayta urinish") { dialog, _ ->
                    dialog.dismiss()
                    onRetry()
                }
                .setNegativeButton("Bekor qilish") { dialog, _ ->
                    dialog.dismiss()
                    onCancel?.invoke()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing retry dialog: ${e.message}", e)
        }
    }

    /**
     * Безопасный вызов с обработкой ошибок
     */
    inline fun <T> safeCall(
        context: Context,
        showDialog: Boolean = true,
        crossinline block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Safe call failed: ${e.message}", e)
            FirebaseManager.crashlytics.recordException(e)

            if (showDialog) {
                handleException(context, e)
            }
            null
        }
    }
}

