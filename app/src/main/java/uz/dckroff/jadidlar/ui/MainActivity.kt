package uz.dckroff.jadidlar.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
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
                R.id.quizListFragment -> {
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
