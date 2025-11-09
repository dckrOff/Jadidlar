package uz.dckroff.jadidlar.ui.jadidlar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.databinding.FragmentJadidlarBinding
import uz.dckroff.jadidlar.ui.adapters.JadidAdapter
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
            val bundle = bundleOf("jadidId" to jadid.id)
            findNavController().navigate(R.id.action_jadidlar_to_jadidDetail, bundle)
        }
        binding.recyclerJadids.adapter = jadidAdapter
    }

    private fun observeData() {
        viewModel.jadids.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    jadidAdapter.submitList(resource.data)
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
