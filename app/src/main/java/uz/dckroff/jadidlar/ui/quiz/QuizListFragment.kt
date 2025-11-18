package uz.dckroff.jadidlar.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentQuizListBinding
import uz.dckroff.jadidlar.ui.adapters.QuizAdapter
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource

class QuizListFragment : Fragment() {
    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizListViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeData()
    }

    private fun setupAdapter() {
        quizAdapter = QuizAdapter { test ->
            try {
                val bundle = bundleOf("testId" to test.id)
                findNavController().navigate(R.id.action_quizList_to_quizSession, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.rvQuiz.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }

    private fun observeData() {
        viewModel.tests.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.shimmerQuiz.visibility = View.VISIBLE
                    binding.rvQuiz.visibility = View.GONE
                    binding.emptyView.visibility = View.GONE
                    binding.errorView.root.visibility = View.GONE
                    startShimmerAnimation()
                }

                is Resource.Success -> {
                    stopShimmerAnimation()
                    binding.shimmerQuiz.visibility = View.GONE
                    try {
                        if (resource.data.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvQuiz.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.rvQuiz.visibility = View.VISIBLE
                            quizAdapter.submitList(resource.data)
                        }
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                is Resource.Error -> {
                    stopShimmerAnimation()
                    binding.shimmerQuiz.visibility = View.GONE
                    binding.rvQuiz.visibility = View.VISIBLE
                    binding.errorView.root.visibility = View.VISIBLE
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Testlar ma'lumotlarini yuklashda xatolik",
                        onRetry = { viewModel.loadTests() }
                    )
                }
            }
        }
    }

    private fun startShimmerAnimation() {
        val container = binding.shimmerQuiz.getChildAt(0) as? ViewGroup ?: return
        for (i in 0 until container.childCount) {
            val shimmerView = container.getChildAt(i) as? io.supercharge.shimmerlayout.ShimmerLayout
            shimmerView?.startShimmerAnimation()
        }
    }

    private fun stopShimmerAnimation() {
        val container = binding.shimmerQuiz.getChildAt(0) as? ViewGroup ?: return
        for (i in 0 until container.childCount) {
            val shimmerView = container.getChildAt(i) as? io.supercharge.shimmerlayout.ShimmerLayout
            shimmerView?.stopShimmerAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
