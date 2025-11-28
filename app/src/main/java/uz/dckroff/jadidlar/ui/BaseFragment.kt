package uz.dckroff.jadidlar.ui

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.utils.ErrorHandler

/**
 * Базовый фрагмент с обработкой ошибок
 */
abstract class BaseFragment : Fragment() {

    /**
     * Безопасная навигация с обработкой ошибок
     */
    protected fun safeNavigate(actionId: Int, args: android.os.Bundle? = null) {
        try {
            findNavController().navigate(actionId, args)
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
        }
    }

    /**
     * Безопасный возврат назад
     */
    protected fun safeNavigateUp() {
        try {
            findNavController().navigateUp()
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e)
        }
    }

    /**
     * Показать ошибку с возможностью повтора
     */
    protected fun showErrorWithRetry(
        message: String = "Ma'lumotlarni yuklashda xatolik",
        onRetry: () -> Unit
    ) {
        ErrorHandler.showErrorWithRetry(
            requireContext(),
            message,
            onRetry = onRetry,
            onCancel = { safeNavigateUp() }
        )
    }

    /**
     * Показать простой диалог с ошибкой
     */
    protected fun showError(
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        ErrorHandler.showErrorDialog(requireContext(), message = message, onDismiss = onDismiss)
    }

    /**
     * Безопасное выполнение с обработкой ошибок
     */
    protected inline fun <T> safeTry(
        showDialog: Boolean = true,
        crossinline block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            if (showDialog) {
                ErrorHandler.handleException(requireContext(), e)
            }
            null
        }
    }
}

