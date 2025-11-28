package uz.dckroff.jadidlar.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.repository.TestRepository
import uz.dckroff.jadidlar.databinding.ActivityMainBinding
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.OnboardingManager
import uz.dckroff.jadidlar.utils.Resource

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var onboardingManager: OnboardingManager
    private val testRepository = TestRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onboardingManager = OnboardingManager(this)
        
        checkAndShowOnboardingQuiz()
    }
    
    private fun checkAndShowOnboardingQuiz() {
        if (!onboardingManager.isOnboardingCompleted()) {
            lifecycleScope.launch {
                try {
                    when (val result = testRepository.getOnboardingQuiz()) {
                        is Resource.Success -> {
                            val onboardingQuiz = result.data
                            if (onboardingQuiz != null) {
                                // Показать вводный тест
                                showOnboardingQuiz(onboardingQuiz.id)
                            } else {
                                // Если вводного теста нет - продолжить нормально
                                setupNavigation()
                            }
                        }
                        is Resource.Error -> {
                            // Ошибка загрузки - предложить повторить или продолжить
                            ErrorHandler.showErrorWithRetry(
                                this@MainActivity,
                                "Kirish testini yuklashda xatolik: ${result.message}",
                                onRetry = { checkAndShowOnboardingQuiz() },
                                onCancel = { setupNavigation() }
                            )
                        }
                        is Resource.Loading -> {
                            // Не должно происходить
                        }
                    }
                } catch (e: Exception) {
                    ErrorHandler.handleException(
                        this@MainActivity,
                        e,
                        "Kirish testini tekshirishda xatolik"
                    ) {
                        setupNavigation()
                    }
                }
            }
        } else {
            // Пользователь уже прошел вводный тест
            setupNavigation()
        }
    }
    
    private fun showOnboardingQuiz(testId: String) {
        // Скрыть bottom navigation для вводного теста
        binding.bottomNavigation.visibility = View.GONE
        
        // Переход к экрану теста с флагом is_onboarding
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val bundle = bundleOf(
            "testId" to testId,
            "isOnboarding" to true
        )
        navController.navigate(R.id.quizSessionFragment, bundle)
        
        // Настроить навигацию после перехода
        setupNavigation()
    }
    
    /**
     * Вызывается когда вводный тест успешно пройден
     */
    fun onOnboardingQuizCompleted() {
        onboardingManager.setOnboardingCompleted()
        
        // Вернуться на главный экран
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        navController.navigate(R.id.homeFragment)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Скрывать/показывать BottomNavigation в зависимости от фрагмента
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Главные экраны - показываем bottom navigation
                R.id.homeFragment,
                R.id.jadidlarFragment,
                R.id.booksFragment,
                R.id.quizListFragment,
                R.id.maxsusTestsListFragment -> {
                    if (binding.bottomNavigation.visibility != View.VISIBLE) {
                        binding.bottomNavigation.visibility = View.VISIBLE
                        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
                        binding.bottomNavigation.startAnimation(slideUp)
                    }
                }
                // Детальные экраны - скрываем bottom navigation
                R.id.bookReaderFragment,
                R.id.bookDetailFragment,
                R.id.jadidDetailFragment,
                R.id.quizSessionFragment,
                R.id.quizResultsFragment -> {
                    if (binding.bottomNavigation.visibility == View.VISIBLE) {
                        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom)
                        binding.bottomNavigation.startAnimation(slideDown)
                        binding.bottomNavigation.visibility = View.GONE
                    }
                }
            }
        }
    }
}
