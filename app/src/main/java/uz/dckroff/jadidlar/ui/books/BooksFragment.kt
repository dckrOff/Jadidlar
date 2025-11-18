package uz.dckroff.jadidlar.ui.books

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentBooksBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.ErrorHandler
import uz.dckroff.jadidlar.utils.Resource

class BooksFragment : Fragment() {
    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BooksViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupListeners()
        observeData()
    }

    private fun setupAdapter() {
        bookAdapter = BookAdapter { book ->
            try {
                val bundle = bundleOf("bookId" to book.id)
                findNavController().navigate(R.id.action_books_to_bookDetail, bundle)
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e, "Sahifaga o'tishda xatolik")
            }
        }
        binding.recyclerBooks.adapter = bookAdapter
    }

    private fun setupListeners() {
        // Связываем SearchBar и SearchView
        binding.searchBar.setOnClickListener {
            try {
                binding.searchView.show()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        // Обрабатываем текст в SearchView
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    s?.toString()?.let { query ->
                        viewModel.searchBooks(query)
                    }
                } catch (e: Exception) {
                    ErrorHandler.handleException(requireContext(), e)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка отправки поиска
        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            try {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchView.text.toString()
                    viewModel.searchBooks(query)
                    binding.searchView.hide()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
                false
            }
        }

        binding.chipAll.setOnClickListener {
            try {
                viewModel.showAll()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.chipRating.setOnClickListener {
            try {
                viewModel.sortByRating()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }

        binding.chipYear.setOnClickListener {
            try {
                viewModel.sortByYear()
            } catch (e: Exception) {
                ErrorHandler.handleException(requireContext(), e)
            }
        }
    }

    private fun observeData() {
        viewModel.books.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    try {
                        if (resource.data.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.recyclerBooks.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.recyclerBooks.visibility = View.VISIBLE
                            bookAdapter.submitList(resource.data)
                        }
                    } catch (e: Exception) {
                        ErrorHandler.handleException(requireContext(), e)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    ErrorHandler.showErrorWithRetry(
                        requireContext(),
                        "Kitoblar ma'lumotlarini yuklashda xatolik",
                        onRetry = { viewModel.loadBooks() }
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
