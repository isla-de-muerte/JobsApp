package com.example.jobsapp.presentation.employer

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
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.databinding.FragmentEmployerVacanciesBinding
import com.example.jobsapp.di.AppModule

class EmployerVacanciesFragment : Fragment() {

    private var _binding: FragmentEmployerVacanciesBinding? = null
    private val binding: FragmentEmployerVacanciesBinding
        get() = requireNotNull(_binding)

    private val viewModel: EmployerViewModel by viewModels {
        AppModule.provideEmployerViewModelFactory(requireContext())
    }

    private var isNavigatingToApplications = false

    private val adapter = EmployerVacancyAdapter(
        onVacancyClick = { vacancy ->
            openEmployerApplications(vacancy)
        },
        onPublishClick = { vacancy ->
            viewModel.publishVacancy(vacancy.id)
        },
        onResumeClick = { vacancy ->
            viewModel.resumeVacancy(vacancy.id)
        },
        onArchiveClick = { vacancy ->
            viewModel.archiveVacancy(vacancy.id)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerVacanciesBinding.inflate(
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
        setupListeners()
        observeState()
        observeActions()

        viewModel.loadMyVacancies()
    }

    override fun onResume() {
        super.onResume()

        isNavigatingToApplications = false
        viewModel.loadMyVacancies()
    }

    private fun setupRecyclerView() {
        binding.employerVacanciesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.employerVacanciesRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.createVacancyButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_employerVacanciesFragment_to_createVacancyFragment
            )
        }

        binding.employerProfileButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_employerVacanciesFragment_to_employerProfileFragment
            )
        }

        binding.logoutButton.setOnClickListener {
            AppModule.provideTokenStorage(requireContext()).clearTokens()

            findNavController().navigate(
                R.id.loginFragment
            )
        }
    }

    private fun openEmployerApplications(
        vacancy: VacancyResponse
    ) {
        if (isNavigatingToApplications) return

        val currentDestinationId = findNavController().currentDestination?.id

        if (currentDestinationId != R.id.employerVacanciesFragment) {
            return
        }

        isNavigatingToApplications = true

        val bundle = Bundle().apply {
            putString("vacancyId", vacancy.id)
            putString("vacancyTitle", vacancy.title)
        }

        findNavController().navigate(
            R.id.employerApplicationsFragment,
            bundle
        )
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                EmployerUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.contentLayout.isVisible = false
                }

                is EmployerUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.contentLayout.isVisible = true

                    adapter.submitList(state.vacancies)
                    renderHiringResponsibilitySummary(state.vacancies)

                    binding.emptyTextView.isVisible = state.vacancies.isEmpty()
                }

                is EmployerUiState.Error -> {
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

    private fun observeActions() {
        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                EmployerActionState.Idle -> Unit

                EmployerActionState.Loading -> Unit

                is EmployerActionState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is EmployerActionState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun renderHiringResponsibilitySummary(
        vacancies: List<VacancyResponse>
    ) {
        val activeVacancies = vacancies.count {
            it.status.name == "PUBLISHED"
        }

        val pausedVacancies = vacancies.count {
            it.status.name == "PAUSED_BY_OVERLOAD"
        }

        val totalApplications = vacancies.sumOf {
            it.applicationsCount
        }

        binding.activeVacanciesTextView.text = activeVacancies.toString()
        binding.pausedVacanciesTextView.text = pausedVacancies.toString()
        binding.totalApplicationsTextView.text = totalApplications.toString()

        binding.hiringResponsibilityHintTextView.text = when {
            pausedVacancies > 0 -> {
                "Есть вакансии с приостановленным приёмом откликов. Обработайте минимум 50% откликов по такой вакансии, чтобы снова открыть её."
            }

            totalApplications == 0 -> {
                "Пока откликов нет. Когда кандидаты начнут откликаться, здесь появится нагрузка по найму."
            }

            else -> {
                "Нагрузка в норме. Продолжайте своевременно обрабатывать отклики кандидатов."
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}