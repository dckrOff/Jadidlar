package uz.dckroff.jadidlar.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Менеджер для управления состоянием прохождения вводного теста
 */
class OnboardingManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Проверяет, был ли пройден вводный тест
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Отмечает вводный тест как пройденный
     */
    fun setOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    /**
     * Сбрасывает состояние вводного теста (для тестирования)
     */
    fun resetOnboarding() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, false)
            .apply()
    }

    /**
     * Проверяет завершенность вводного теста с учетом версии
     * @param version версия вводного теста
     */
    fun isOnboardingCompleted(version: Int): Boolean {
        return prefs.getInt(KEY_ONBOARDING_VERSION, 0) >= version
    }

    /**
     * Устанавливает версию пройденного вводного теста
     * @param version версия вводного теста
     */
    fun setOnboardingCompleted(version: Int) {
        prefs.edit()
            .putInt(KEY_ONBOARDING_VERSION, version)
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_quiz_completed"
        private const val KEY_ONBOARDING_VERSION = "onboarding_quiz_version"
    }
}

