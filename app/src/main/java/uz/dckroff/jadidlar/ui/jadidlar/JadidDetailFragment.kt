package uz.dckroff.jadidlar.ui.jadidlar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentJadidDetailBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.FavoriteType
import uz.dckroff.jadidlar.utils.FavoritesManager
import uz.dckroff.jadidlar.utils.Resource
import uz.dckroff.jadidlar.utils.loadImageSafe

class JadidDetailFragment : Fragment() {
    private var _binding: FragmentJadidDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JadidDetailViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var favoritesManager: FavoritesManager
    private var currentJadidId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJadidDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            favoritesManager = FavoritesManager(requireContext())

            val jadidId = arguments?.getString("jadidId")
            if (jadidId != null) {
                currentJadidId = jadidId
                viewModel.loadJadid(jadidId)
                AnalyticsHelper.logJadidViewed(requireContext(), jadidId)
            } else {
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Jadid ma'lumotlari topilmadi"
                ) {
                    findNavController().navigateUp()
                }
            }

            setupAdapter()
            setupListeners()
            observeData()
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e) {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupAdapter() {
        bookAdapter = BookAdapter { book ->
            try {
                val bundle = bundleOf("bookId" to book.id)
                findNavController().navigate(R.id.action_jadidDetail_to_bookDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerAsarlar.adapter = bookAdapter
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.buttonMore.setOnClickListener {
            try {
                viewModel.toggleDescription()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }
    }

    private fun observeData() {
        viewModel.jadid.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    try {
                        val jadid = resource.data
                        binding.textName.text = jadid.name
                        binding.textYears.text = "(${jadid.birthYear} – ${jadid.deathYear})"
                        binding.textBiography.text = jadid.shortDescription
                        binding.imagePortrait.loadImageSafe(jadid.imageUrl)
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                is Resource.Error -> {
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Jadid ma'lumotlarini yuklashda xatolik \n " + resource.message,
                        onRetry = { currentJadidId?.let { viewModel.loadJadid(it) } },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }

        viewModel.books.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    try {
                        bookAdapter.submitList(resource.data)
                        binding.recyclerAsarlar.visibility =
                            if (resource.data.isEmpty()) View.GONE else View.VISIBLE
                        binding.textAsarlarHeader.visibility =
                            if (resource.data.isEmpty()) View.GONE else View.VISIBLE
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                is Resource.Error -> {
                    // Книги - это необязательная информация, не показываем диалог
                }
            }
        }

        viewModel.isDescriptionExpanded.observe(viewLifecycleOwner) { isExpanded ->
            try {
                binding.textBiography.maxLines = if (isExpanded) Int.MAX_VALUE else 5
                binding.buttonMore.text = if (isExpanded) "Qisqartirish" else "Batafsil"

                if (isExpanded) {
                    viewModel.jadid.value?.let { resource ->
                        if (resource is Resource.Success) {
                            binding.textBiography.text = resource.data.fullDescription
                        }
                    }
                } else {
                    viewModel.jadid.value?.let { resource ->
                        if (resource is Resource.Success) {
                            binding.textBiography.text = resource.data.shortDescription
                        }
                    }
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
