package com.example.jobsapp.presentation.vacancies

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.databinding.FragmentVacancyDetailsBinding
import com.example.jobsapp.di.AppModule

class VacancyDetailsFragment : Fragment() {

    private var _binding: FragmentVacancyDetailsBinding? = null
    private val binding: FragmentVacancyDetailsBinding
        get() = requireNotNull(_binding)

    private val viewModel: VacancyViewModel by viewModels {
        AppModule.provideVacancyViewModelFactory(requireContext())
    }

    private lateinit var vacancyId: String
    private var currentVacancy: VacancyResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vacancyId = requireArguments().getString("vacancyId")
            ?: error("vacancyId is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyDetailsBinding.inflate(
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

        setupListeners()
        observeDetails()
        observeActions()

        viewModel.loadVacancyDetails(vacancyId)
    }

    private fun setupListeners() {
        binding.backTextView.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.applyButton.setOnClickListener {
            showApplyDialog()
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.addFavorite(vacancyId)
        }
    }

    private fun observeDetails() {
        viewModel.detailsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                VacancyDetailsUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.contentScrollView.isVisible = false
                    binding.bottomActionsLayout.isVisible = false
                }

                is VacancyDetailsUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.contentScrollView.isVisible = true
                    binding.bottomActionsLayout.isVisible = true

                    currentVacancy = state.vacancy
                    renderVacancy(state.vacancy)
                }

                is VacancyDetailsUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.contentScrollView.isVisible = false
                    binding.bottomActionsLayout.isVisible = false

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
                VacancyActionUiState.Idle -> {
                    setButtonsEnabled(true)
                }

                VacancyActionUiState.Loading -> {
                    setButtonsEnabled(false)
                }

                is VacancyActionUiState.Success -> {
                    setButtonsEnabled(true)

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()

                    viewModel.loadVacancyDetails(vacancyId)
                }

                is VacancyActionUiState.Error -> {
                    setButtonsEnabled(true)

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun renderVacancy(vacancy: VacancyResponse) {
        binding.titleTextView.text = vacancy.title
        binding.salaryTextView.text = formatSalary(vacancy)
        binding.workFormatTextView.text = mapWorkFormat(vacancy.workFormat.name)
        binding.statusTextView.text = mapStatus(vacancy.status.name)

        binding.viewsTextView.text = vacancy.viewsCount.toString()
        binding.applicationsTextView.text = vacancy.applicationsCount.toString()

        binding.descriptionTextView.text = vacancy.description

        val requirements = vacancy.requirements
        binding.requirementsCard.isVisible = !requirements.isNullOrBlank()
        binding.requirementsTextView.text = requirements.orEmpty()
    }

    private fun showApplyDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Кратко расскажите, почему вы подходите"
            minLines = 4
            maxLines = 8
            setPadding(32, 24, 32, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Отклик на вакансию")
            .setMessage(currentVacancy?.title.orEmpty())
            .setView(editText)
            .setPositiveButton("Отправить") { _, _ ->
                viewModel.applyToVacancy(
                    vacancyId = vacancyId,
                    coverLetter = editText.text.toString()
                        .ifBlank { "Здравствуйте! Хочу откликнуться на вакансию." }
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.applyButton.isEnabled = enabled
        binding.favoriteButton.isEnabled = enabled
    }

    private fun formatSalary(vacancy: VacancyResponse): String {
        val from = vacancy.salaryFrom
        val to = vacancy.salaryTo

        return when {
            from != null && to != null -> "$from - $to ₽"
            from != null -> "от $from ₽"
            to != null -> "до $to ₽"
            else -> "Зарплата не указана"
        }
    }

    private fun mapWorkFormat(value: String): String {
        return when (value) {
            "REMOTE" -> "Удалённо"
            "PART_TIME" -> "Подработка"
            "HYBRID" -> "Гибрид"
            "OFFICE" -> "Офис"
            else -> value
        }
    }

    private fun mapStatus(value: String): String {
        return when (value) {
            "PUBLISHED" -> "Опубликована"
            "DRAFT" -> "Черновик"
            "ARCHIVED" -> "Архив"
            else -> value
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}