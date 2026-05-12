package com.example.jobsapp.presentation.vacancies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobsapp.R
import com.example.jobsapp.databinding.FragmentVacancyListBinding
import com.example.jobsapp.di.AppModule

class VacancyListFragment : Fragment() {

    private var _binding: FragmentVacancyListBinding? = null
    private val binding: FragmentVacancyListBinding
        get() = requireNotNull(_binding)

    private val viewModel: VacancyViewModel by viewModels {
        AppModule.provideVacancyViewModelFactory(requireContext())
    }

    private val adapter = VacancyAdapter(
        onClick = { vacancy ->
            val bundle = Bundle().apply {
                putString("vacancyId", vacancy.id)
            }

            findNavController().navigate(
                R.id.action_vacancyListFragment_to_vacancyDetailsFragment,
                bundle
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyListBinding.inflate(
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
        setupSearch()
        setupFilters()
        observeState()

        viewModel.loadVacancies()
    }

    private fun setupRecyclerView() {
        binding.vacanciesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.vacanciesRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                viewModel.search(
                    binding.searchEditText.text.toString()
                )
                true
            } else {
                false
            }
        }
    }

    private fun setupFilters() {
        binding.workFormatRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val workFormat = when (checkedId) {
                R.id.remoteRadioButton -> "REMOTE"
                R.id.hybridRadioButton -> "HYBRID"
                R.id.officeRadioButton -> "OFFICE"
                R.id.partTimeRadioButton -> "PART_TIME"
                else -> null
            }

            viewModel.filterByWorkFormat(workFormat)
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                VacancyListUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.emptyTextView.isVisible = false
                }

                is VacancyListUiState.Success -> {
                    binding.progressBar.isVisible = false

                    adapter.submitList(state.vacancies)

                    binding.emptyTextView.isVisible =
                        state.vacancies.isEmpty()
                }

                is VacancyListUiState.Error -> {
                    binding.progressBar.isVisible = false
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