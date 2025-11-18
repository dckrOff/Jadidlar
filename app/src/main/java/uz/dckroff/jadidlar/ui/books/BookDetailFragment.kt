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
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentBookDetailBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.AnalyticsHelper
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource
import uz.dckroff.jadidlar.utils.loadImageSafe

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

        try {
            currentBookId = arguments?.getString("bookId")
            if (currentBookId != null) {
                viewModel.loadBook(currentBookId!!, requireContext())
            } else {
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Kitob ma'lumotlari topilmadi"
                ) {
                    findNavController().navigateUp()
                }
            }

            setupAdapter()
            setupListeners()
            setupMenu()
            observeData()
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e) {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupAdapter() {
        otherBooksAdapter = BookAdapter { book ->
            try {
                val bundle = bundleOf("bookId" to book.id)
                findNavController().navigate(R.id.action_bookDetail_to_bookDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerOtherBooks.adapter = otherBooksAdapter
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.buttonStartReading.setOnClickListener {
            try {
                val book = (viewModel.book.value as? Resource.Success)?.data
                if (book != null) {
                    AnalyticsHelper.logReadingStarted(requireContext(), book.id)

                    val bundle = bundleOf(
                        "bookId" to book.id,
                        "bookTitle" to book.title,
                        "pdfUrl" to book.pdfUrl
                    )
                    findNavController().navigate(R.id.action_bookDetail_to_reader, bundle)
                } else {
                    ErrorHandler.showErrorDialog(requireContext(), message = "Kitob ma'lumotlari topilmadi")
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "O'qishni boshlashda xatolik")
            }
        }

        binding.textAuthorName.setOnClickListener {
            try {
                val book = (viewModel.book.value as? Resource.Success)?.data
                if (book != null) {
                    val bundle = bundleOf("jadidId" to book.authorId)
                    findNavController().navigate(R.id.action_bookDetail_to_jadidDetail, bundle)
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.book_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return try {
                    when (menuItem.itemId) {
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
                            } else {
                                ErrorHandler.showErrorDialog(requireContext(), message = "Kitob ma'lumotlari topilmadi")
                            }
                            true
                        }

                        else -> false
                    }
                } catch (e: Exception) {
                    ErrorHandler.handleException(requireContext(), e)
                    false
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
                    try {
                        displayBook(resource.data)
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Kitob ma'lumotlarini yuklashda xatolik \n " + resource.message,
                        onRetry = { currentBookId?.let { viewModel.loadBook(it, requireContext()) } },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }

        viewModel.otherBooks.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    try {
                        if (resource.data.isNotEmpty()) {
                            binding.textOtherBooksHeader.visibility = View.VISIBLE
                            binding.recyclerOtherBooks.visibility = View.VISIBLE
                            otherBooksAdapter.submitList(resource.data)
                        }
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                else -> {}
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            // Не показываем toast, так как это не критично
        }
    }

    private fun displayBook(book: uz.dckroff.jadidlar.data.models.Book) {
        binding.textBookTitle.text = book.title
        binding.textAuthorName.text = book.authorName
        binding.textPublishYear.text = "${book.publishYear}-yil"
        binding.textRating.text = "${book.rating} o'qilgan"
        binding.textDescription.text = book.description
        binding.imageBookCover.loadImageSafe(book.coverImageUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
