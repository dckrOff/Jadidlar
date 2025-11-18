package uz.dckroff.jadidlar.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentQuizResultsBinding
import uz.dckroff.jadidlar.ui.adapters.QuestionResultAdapter
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource
import java.util.Locale

class QuizResultsFragment : Fragment() {
    private var _binding: FragmentQuizResultsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: QuizResultsViewModel by viewModels()
    private lateinit var resultsAdapter: QuestionResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val testId = arguments?.getString("testId")
            if (testId == null) {
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Test ma'lumotlari topilmadi"
                ) {
                    findNavController().navigateUp()
                }
                return
            }
            
            val score = arguments?.getInt("score") ?: 0
            val totalQuestions = arguments?.getInt("totalQuestions") ?: 0
            val timeSpent = arguments?.getLong("timeSpent") ?: 0
            
            setupAdapter()
            setupListeners(testId)
            displayResults(score, totalQuestions, timeSpent)
            
            val userAnswers = mutableMapOf<Int, Int>()
            viewModel.loadTestResults(testId, userAnswers)
            viewModel.saveResult(testId, score, totalQuestions, timeSpent, userAnswers)
            
            observeData()
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e) {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupAdapter() {
        resultsAdapter = QuestionResultAdapter()
        binding.questionsRecyclerView.adapter = resultsAdapter
    }

    private fun setupListeners(testId: String) {
        binding.toolbar.setNavigationOnClickListener {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.retakeQuizButton.setOnClickListener {
            try {
                val bundle = bundleOf("testId" to testId)
                findNavController().navigate(R.id.action_quizSession_to_results, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Testni qayta boshlashda xatolik")
            }
        }
    }

    private fun displayResults(score: Int, totalQuestions: Int, timeSpent: Long) {
        val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
        
        binding.tvScore.text = "$percentage%"
        binding.correctAnswersTextView.text = "$totalQuestions dan $score to'g'ri"
        
        val minutes = (timeSpent / 1000) / 60
        val seconds = (timeSpent / 1000) % 60
        binding.timeSpentTextView.text = String.format(
            Locale.getDefault(),
            "Sarflangan vaqt: %02d:%02d",
            minutes,
            seconds
        )
    }

    private fun observeData() {
        viewModel.test.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.quizTitleTextView.text = resource.data.title
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.questionResults.observe(viewLifecycleOwner) { results ->
            resultsAdapter.submitList(results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
