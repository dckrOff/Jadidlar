package uz.dckroff.jadidlar.ui.books

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rajat.pdfviewer.PdfRendererView
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.databinding.FragmentBookReaderBinding
import uz.dckroff.jadidlar.utils.DownloadUtils
import uz.dckroff.jadidlar.utils.ErrorHandler
import java.io.File

class BookReaderFragment : Fragment() {
    private var _binding: FragmentBookReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookReaderViewModel by viewModels()
    private var bookId: String? = null
    private var totalPages: Int = 0
    private var currentPageNumber: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            bookId = arguments?.getString("bookId")
            val bookTitle = arguments?.getString("bookTitle")
            val pdfUrl = arguments?.getString("pdfUrl")

            binding.toolbar.title = bookTitle
            binding.toolbar.setNavigationOnClickListener {
                try {
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    ErrorHandler.handleException(requireContext(), e)
                }
            }

            if (bookId != null && pdfUrl != null) {
                viewModel.initialize(requireContext(), bookId!!)
                loadPdf(pdfUrl)
            } else {
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Kitob ma'lumotlari topilmadi"
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

    private fun loadPdf(url: String) {
        try {
            if (bookId == null) {
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                ErrorHandler.showErrorDialog(
                    requireContext(),
                    message = "Kitob ID topilmadi"
                ) {
                    findNavController().navigateUp()
                }
                return
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val cacheFile = DownloadUtils.getPdfCacheFile(requireContext(), bookId!!)

                    if (cacheFile.exists() && cacheFile.length() > 0) {
                        openPdfFromCache(cacheFile)
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        val downloadedFile =
                            DownloadUtils.downloadPdfToCache(requireContext(), url, bookId!!)

                        if (downloadedFile != null && downloadedFile.exists()) {
                            openPdfFromCache(downloadedFile)
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.errorView.visibility = View.VISIBLE
                            Toast.makeText(
                                requireContext(),
                                "PDF yuklashda xatolik yuz berdi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    try {
                        binding.progressBar.visibility = View.GONE
                        binding.errorView.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            "Xatolik: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e("==BookReaderFragment==", "loadPdf: catch{} error")
                    }
                }
            }
        } catch (e: Exception) {
            ErrorHandler.handleException(requireContext(), e, "PDF yuklashda xatolik")
        }
    }

    private fun openPdfFromCache(pdfFile: File) {
        try {
            binding.pdfView.initWithFile(file = pdfFile)

            binding.pdfView.statusListener = object : PdfRendererView.StatusCallBack {
                override fun onPdfLoadStart() {
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPdfLoadProgress(
                    progress: Int,
                    downloadedBytes: Long,
                    totalBytes: Long?
                ) {
                }

                override fun onPdfLoadSuccess(absolutePath: String) {
                    binding.progressBar.visibility = View.GONE
                    totalPages = binding.pdfView.totalPageCount
                    binding.pageSlider.valueTo = totalPages.toFloat()

                    viewModel.currentPage.value?.let { savedPage ->
                        if (savedPage >= 0 && savedPage < totalPages) {
                            binding.pdfView.jumpToPage(savedPage)
                            currentPageNumber = savedPage
                        }
                    }

                    updatePageNumber()
                }

                override fun onError(error: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "PDF ochishda xatolik: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    currentPageNumber = currentPage
                    bookId?.let { viewModel.saveCurrentPage(it, currentPage) }
                    updatePageNumber()
                }

                override fun onPdfRenderStart() {
                }

                override fun onPdfRenderSuccess() {
                }
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.errorView.visibility = View.VISIBLE
            Toast.makeText(
                requireContext(),
                "PDF ochishda xatolik: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setupListeners() {
        binding.buttonPreviousPage.setOnClickListener {
            if (currentPageNumber > 0) {
                binding.pdfView.jumpToPage(currentPageNumber - 1)
            }
        }

        binding.buttonNextPage.setOnClickListener {
            if (currentPageNumber < totalPages - 1) {
                binding.pdfView.jumpToPage(currentPageNumber + 1)
            }
        }

        binding.pageSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val page = value.toInt() - 1
                binding.pdfView.jumpToPage(page)
            }
        }
    }

    fun observeData() {
        viewModel.currentPage.observe(viewLifecycleOwner) { page ->
            binding.pageSlider.value = (page + 1).toFloat()
        }
    }

    fun updatePageNumber() {
        val displayPage = currentPageNumber + 1
        binding.textPageNumber.text = "$displayPage / $totalPages sahifa"
        binding.pageSlider.value = displayPage.toFloat()

        binding.buttonPreviousPage.isEnabled = currentPageNumber > 0
        binding.buttonNextPage.isEnabled = currentPageNumber < totalPages - 1
    }

    override fun onPause() {
        super.onPause()
        bookId?.let { viewModel.saveCurrentPage(it, currentPageNumber) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}