package uz.dckroff.jadidlar.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentBooksBinding
import uz.dckroff.jadidlar.ui.adapters.BookAdapter
import uz.dckroff.jadidlar.utils.Resource

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo

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
            val bundle = bundleOf("bookId" to book.id)
            findNavController().navigate(R.id.action_books_to_bookDetail, bundle)
        }
        binding.recyclerBooks.adapter = bookAdapter
    }

    private fun setupListeners() {
        // Связываем SearchBar и SearchView
        binding.searchBar.setOnClickListener {
            binding.searchView.show()
        }

        // Обрабатываем текст в SearchView
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { query ->
                    viewModel.searchBooks(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка отправки поиска
        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString()
                viewModel.searchBooks(query)
                binding.searchView.hide()
                true
            } else {
                false
            }
        }

        binding.chipAll.setOnClickListener {
            viewModel.showAll()
        }

        binding.chipRating.setOnClickListener {
            viewModel.sortByRating()
        }

        binding.chipYear.setOnClickListener {
            viewModel.sortByYear()
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
                    if (resource.data.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.recyclerBooks.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.recyclerBooks.visibility = View.VISIBLE
                        bookAdapter.submitList(resource.data)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
