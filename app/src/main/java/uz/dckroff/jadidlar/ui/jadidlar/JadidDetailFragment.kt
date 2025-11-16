package uz.dckroff.jadidlar.ui.jadidlar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentJadidDetailBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.FavoriteType
import uz.dckroff.jadidlar.utils.FavoritesManager
import uz.dckroff.jadidlar.utils.Resource

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

        favoritesManager = FavoritesManager(requireContext())

        val jadidId = arguments?.getString("jadidId")
        if (jadidId != null) {
            currentJadidId = jadidId
            viewModel.loadJadid(jadidId)
            AnalyticsHelper.logJadidViewed(requireContext(), jadidId)
        }

        setupAdapter()
        setupListeners()
        observeData()
    }

    private fun setupAdapter() {
        bookAdapter = BookAdapter { book ->
            val bundle = bundleOf("bookId" to book.id)
            findNavController().navigate(R.id.action_jadidDetail_to_bookDetail, bundle)
        }
        binding.recyclerAsarlar.adapter = bookAdapter
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonMore.setOnClickListener {
            viewModel.toggleDescription()
        }
    }

    private fun observeData() {
        viewModel.jadid.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val jadid = resource.data
                    binding.textName.text = jadid.name
                    binding.textYears.text = "(${jadid.birthYear} – ${jadid.deathYear})"
                    binding.textBiography.text = jadid.shortDescription

                    Glide.with(this)
                        .load(jadid.imageUrl)
                        .placeholder(R.drawable.img_placeholder)
                        .error(R.drawable.img_placeholder)
                        .   into(binding.imagePortrait)
                }

                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.books.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    bookAdapter.submitList(resource.data)
                    binding.recyclerAsarlar.visibility =
                        if (resource.data.isEmpty()) View.GONE else View.VISIBLE
                    binding.textAsarlarHeader.visibility =
                        if (resource.data.isEmpty()) View.GONE else View.VISIBLE
                }

                is Resource.Error -> {}
            }
        }

        viewModel.isDescriptionExpanded.observe(viewLifecycleOwner) { isExpanded ->
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
