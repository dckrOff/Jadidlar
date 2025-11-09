package uz.dckroff.jadidlar.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentBookDetailBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.Resource

class BookDetailFragment : Fragment() {
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BookDetailViewModel by viewModels()
    private lateinit var otherBooksAdapter: BookAdapter
    private var currentBookId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        currentBookId = arguments?.getString("bookId")
        if (currentBookId != null) {
            viewModel.loadBook(currentBookId!!, requireContext())
        }
        
        setupAdapter()
        setupListeners()
        setupMenu()
        observeData()
    }

    private fun setupAdapter() {
        otherBooksAdapter = BookAdapter { book ->
            val bundle = bundleOf("bookId" to book.id)
            findNavController().navigate(R.id.action_bookDetail_to_bookDetail, bundle)
        }
        binding.recyclerOtherBooks.adapter = otherBooksAdapter
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonStartReading.setOnClickListener {
            val book = (viewModel.book.value as? Resource.Success)?.data ?: return@setOnClickListener
            
            AnalyticsHelper.logReadingStarted(requireContext(), book.id)
            
            val bundle = bundleOf(
                "bookId" to book.id,
                "bookTitle" to book.title,
                "pdfUrl" to book.pdfUrl
            )
            findNavController().navigate(R.id.action_bookDetail_to_reader, bundle)
        }

        binding.textAuthorName.setOnClickListener {
            val book = (viewModel.book.value as? Resource.Success)?.data ?: return@setOnClickListener
            val bundle = bundleOf("jadidId" to book.authorId)
            findNavController().navigate(R.id.action_bookDetail_to_jadidDetail, bundle)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.book_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_favorite -> {
                        currentBookId?.let {
                            viewModel.toggleFavorite(it, requireContext())
                        }
                        true
                    }
                    R.id.action_download -> {
                        val book = (viewModel.book.value as? Resource.Success)?.data
                        if (book != null) {
                            viewModel.downloadBook(
                                requireContext(),
                                book.pdfUrl,
                                "${book.title}.pdf"
                            )
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.download_started),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeData() {
        viewModel.book.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    displayBook(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.otherBooks.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data.isNotEmpty()) {
                        binding.textOtherBooksHeader.visibility = View.VISIBLE
                        binding.recyclerOtherBooks.visibility = View.VISIBLE
                        otherBooksAdapter.submitList(resource.data)
                    }
                }
                else -> {}
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            val message = if (isFavorite) {
                getString(R.string.added_to_favorites)
            } else {
                getString(R.string.removed_from_favorites)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayBook(book: uz.dckroff.jadidlar.data.models.Book) {
        binding.textBookTitle.text = book.title
        binding.textAuthorName.text = book.authorName
        binding.textPublishYear.text = "${book.publishYear}-yil"
        binding.textRating.text = "${book.rating} o'qilgan"
        binding.textDescription.text = book.description
        
        Glide.with(this)
            .load(book.coverImageUrl)
            .placeholder(R.drawable.sample_book)
            .error(R.drawable.sample_book)
            .centerCrop()
            .into(binding.imageBookCover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
