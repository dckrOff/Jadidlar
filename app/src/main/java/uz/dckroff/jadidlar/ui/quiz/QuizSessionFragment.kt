package uz.dckroff.jadidlar.ui.quiz

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.models.Question
import uz.dckroff.jadidlar.databinding.FragmentQuizSessionBinding
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource
import java.util.Locale

class QuizSessionFragment : Fragment() {
    private var _binding: FragmentQuizSessionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: QuizSessionViewModel by viewModels()
    private var testId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            testId = arguments?.getString("testId")
            if (testId != null) {
                viewModel.loadTest(testId!!)
                AnalyticsHelper.logTestStarted(requireContext(), testId!!)
            } else {
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Test ma'lumotlari topilmadi"
                ) {
                    findNavController().navigateUp()
                }
            }
            
            setupListeners()
            observeData()
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e) {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            try {
                showExitDialog()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.previousButton.setOnClickListener {
            try {
                viewModel.previousQuestion()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.nextButton.setOnClickListener {
            try {
                val selectedAnswer = getSelectedAnswerIndex()
                if (selectedAnswer == -1) {
                    Toast.makeText(requireContext(), "Iltimos javob tanlang", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.saveAnswer(selectedAnswer)
                viewModel.nextQuestion()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.finishButton.setOnClickListener {
            try {
                val selectedAnswer = getSelectedAnswerIndex()
                if (selectedAnswer == -1) {
                    Toast.makeText(requireContext(), "Iltimos javob tanlang", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.saveAnswer(selectedAnswer)
                showFinishDialog()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }
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
                    binding.errorView.root.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            displayQuestion(question)
        }

        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            updateProgress(index)
            updateNavigationButtons(index)
            restoreSelectedAnswer()
        }

        viewModel.timeRemaining.observe(viewLifecycleOwner) { timeMillis ->
            val minutes = (timeMillis / 1000) / 60
            val seconds = (timeMillis / 1000) % 60
            binding.timerTextView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            
            if (timeMillis == 0L) {
                finishTest()
            }
        }
    }

    private fun displayQuestion(question: Question) {
        binding.questionTextView.text = question.questionText
        binding.singleChoiceGroup.removeAllViews()
        binding.singleChoiceGroup.visibility = View.VISIBLE
        
        question.answers.forEachIndexed { index, answer ->
            val radioButton = RadioButton(requireContext()).apply {
                text = answer
                id = index
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.singleChoiceGroup.addView(radioButton)
        }
    }

    private fun updateProgress(currentIndex: Int) {
        val test = (viewModel.test.value as? Resource.Success)?.data ?: return
        val totalQuestions = test.questions.size
        val progress = ((currentIndex + 1) * 100) / totalQuestions
        
        binding.progressIndicator.progress = progress
        binding.progressTextView.text = "${currentIndex + 1}/$totalQuestions savolga javob berildi"
        binding.questionNumberTextView.text = "Savol ${currentIndex + 1}/$totalQuestions"
    }

    private fun updateNavigationButtons(currentIndex: Int) {
        val test = (viewModel.test.value as? Resource.Success)?.data ?: return
        val totalQuestions = test.questions.size
        
        binding.previousButton.isEnabled = currentIndex > 0
        binding.nextButton.visibility = if (currentIndex < totalQuestions - 1) View.VISIBLE else View.GONE
        binding.finishButton.visibility = if (currentIndex == totalQuestions - 1) View.VISIBLE else View.GONE
    }

    private fun getSelectedAnswerIndex(): Int {
        val selectedId = binding.singleChoiceGroup.checkedRadioButtonId
        return if (selectedId != -1) selectedId else -1
    }

    private fun restoreSelectedAnswer() {
        val savedAnswer = viewModel.getSelectedAnswer()
        if (savedAnswer != null && savedAnswer != -1) {
            binding.singleChoiceGroup.check(savedAnswer)
        } else {
            binding.singleChoiceGroup.clearCheck()
        }
    }

    private fun showFinishDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Testni yakunlash")
            .setMessage("Testni yakunlashni xohlaysizmi?")
            .setPositiveButton("Ha") { _, _ ->
                finishTest()
            }
            .setNegativeButton("Yo'q", null)
            .show()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Testni tark etish")
            .setMessage("Testni tark etsangiz, natijalar saqlanmaydi. Davom etishni xohlaysizmi?")
            .setPositiveButton("Tark etish") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("Qaytish", null)
            .show()
    }

    private fun finishTest() {
        val (score, totalQuestions, timeSpent) = viewModel.calculateResult()
        
        testId?.let {
            AnalyticsHelper.logTestCompleted(requireContext(), it, score, timeSpent)
        }
        
        val bundle = bundleOf(
            "testId" to testId,
            "score" to score,
            "totalQuestions" to totalQuestions,
            "timeSpent" to timeSpent
        )
        findNavController().navigate(R.id.action_quizSession_to_results, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
