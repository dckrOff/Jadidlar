package uz.dckroff.jadidlar.ui.jadidlar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentJadidlarBinding
import uz.dckroff.jadidlar.ui.adapters.JadidAdapter
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource

class JadidlarFragment : Fragment() {
    private var _binding: FragmentJadidlarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: JadidlarViewModel by viewModels()
    private lateinit var jadidAdapter: JadidAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJadidlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeData()
    }

    private fun setupAdapter() {
        jadidAdapter = JadidAdapter { jadid ->
            try {
                val bundle = bundleOf("jadidId" to jadid.id)
                findNavController().navigate(R.id.action_jadidlar_to_jadidDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerJadids.adapter = jadidAdapter
    }

    private fun observeData() {
        viewModel.jadids.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.shimmerContainer.visibility = View.VISIBLE
                    binding.recyclerJadids.visibility = View.GONE
                    startShimmerAnimation()
                }
                is Resource.Success -> {
                    stopShimmerAnimation()
                    binding.shimmerContainer.visibility = View.GONE
                    binding.recyclerJadids.visibility = View.VISIBLE
                    try {
                        jadidAdapter.submitList(resource.data)
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }
                is Resource.Error -> {
                    stopShimmerAnimation()
                    binding.shimmerContainer.visibility = View.GONE
                    binding.recyclerJadids.visibility = View.VISIBLE
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Jadidlar ma'lumotlarini yuklashda xatolik",
                        onRetry = { viewModel.loadAllJadids() }
                    )
                }
            }
        }
    }

    private fun startShimmerAnimation() {
        val container = binding.shimmerContainer.getChildAt(0) as ViewGroup
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as? ViewGroup ?: continue
            for (j in 0 until row.childCount) {
                val shimmerView = row.getChildAt(j) as? io.supercharge.shimmerlayout.ShimmerLayout
                shimmerView?.startShimmerAnimation()
            }
        }
    }

    private fun stopShimmerAnimation() {
        val container = binding.shimmerContainer.getChildAt(0) as? ViewGroup ?: return
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as? ViewGroup ?: continue
            for (j in 0 until row.childCount) {
                val shimmerView = row.getChildAt(j) as? io.supercharge.shimmerlayout.ShimmerLayout
                shimmerView?.stopShimmerAnimation()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
