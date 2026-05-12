package com.example.jobsapp.presentation.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobsapp.R
import com.example.jobsapp.databinding.FragmentApplicationsBinding
import com.example.jobsapp.di.AppModule

class ApplicationsFragment : Fragment() {

    private var _binding: FragmentApplicationsBinding? = null
    private val binding: FragmentApplicationsBinding
        get() = requireNotNull(_binding)

    private val viewModel: ApplicationsViewModel by viewModels {
        AppModule.provideApplicationsViewModelFactory(requireContext())
    }

    private val adapter = ApplicationsAdapter(
        onClick = { item ->
            openChat(item)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicationsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeState()

        viewModel.loadApplications()
    }

    private fun setupRecyclerView() {
        binding.applicationsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.applicationsRecyclerView.adapter = adapter
    }

    private fun openChat(
        item: ApplicationUiModel
    ) {
        val bundle = Bundle().apply {
            putString("applicationId", item.application.id)
            putString("title", item.vacancyTitle)
        }

        findNavController().navigate(
            R.id.applicationChatFragment,
            bundle
        )
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ApplicationsUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.contentLayout.isVisible = false
                }

                is ApplicationsUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.contentLayout.isVisible = true

                    adapter.submitList(state.applications)
                    binding.emptyTextView.isVisible = state.applications.isEmpty()
                }

                is ApplicationsUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.contentLayout.isVisible = true
                    binding.emptyTextView.isVisible = true

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}