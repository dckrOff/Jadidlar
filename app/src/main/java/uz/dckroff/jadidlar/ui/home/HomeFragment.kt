package uz.dckroff.jadidlar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentHomeBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.ui.adapters.JadidAdapter
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var jadidAdapter: JadidAdapter
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeData()
    }

    private fun setupAdapters() {
        jadidAdapter = JadidAdapter { jadid ->
            try {
                val bundle = bundleOf("jadidId" to jadid.id)
                findNavController().navigate(R.id.action_home_to_jadidDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerJadidlar.adapter = jadidAdapter

        bookAdapter = BookAdapter { book ->
            try {
                val bundle = bundleOf("bookId" to book.id)
                findNavController().navigate(R.id.action_home_to_bookDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerTopAsarlar.adapter = bookAdapter
    }

    private fun observeData() {
        viewModel.jadids.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    try {
                        jadidAdapter.submitList(resource.data)
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }
                is Resource.Error -> {
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Jadidlar ma'lumotlarini yuklashda xatolik",
                        onRetry = { viewModel.loadData() }
                    )
                }
            }
        }

        viewModel.topBooks.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    try {
                        bookAdapter.submitList(resource.data)
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }
                is Resource.Error -> {
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Kitoblar ma'lumotlarini yuklashda xatolik",
                        onRetry = { viewModel.loadData() }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
