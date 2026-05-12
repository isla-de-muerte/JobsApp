package com.example.jobsapp.presentation.employer

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobsapp.R
import com.example.jobsapp.data.dto.ApplicationStatusDto
import com.example.jobsapp.databinding.FragmentEmployerApplicationsBinding
import com.example.jobsapp.di.AppModule

class EmployerApplicationsFragment : Fragment() {

    private var _binding: FragmentEmployerApplicationsBinding? = null
    private val binding: FragmentEmployerApplicationsBinding
        get() = requireNotNull(_binding)

    private val viewModel: EmployerViewModel by viewModels {
        AppModule.provideEmployerViewModelFactory(requireContext())
    }

    private lateinit var vacancyId: String
    private lateinit var vacancyTitle: String

    private val adapter = EmployerApplicationsAdapter(
        onApplicationClick = { item ->
            openChat(item)
        },
        onStatusClick = { item, status ->
            showStatusMessageDialog(
                item = item,
                status = status
            )
        }
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        vacancyId = requireArguments().getString("vacancyId")
            ?: error("vacancyId is required")

        vacancyTitle = requireArguments().getString("vacancyTitle")
            ?: "Отклики"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerApplicationsBinding.inflate(
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

        binding.titleTextView.text = "Отклики"
        binding.subtitleTextView.text = vacancyTitle

        setupRecyclerView()
        setupListeners()
        observeApplications()
        observeActions()

        viewModel.loadApplicationsByVacancy(vacancyId)
    }

    override fun onResume() {
        super.onResume()

        if (::vacancyId.isInitialized) {
            viewModel.loadApplicationsByVacancy(vacancyId)
        }
    }

    private fun setupRecyclerView() {
        binding.applicationsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.applicationsRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.backTextView.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun openChat(
        item: EmployerApplicationUiModel
    ) {
        val application = item.application

        val bundle = Bundle().apply {
            putString("applicationId", application.id)
            putString("title", application.resume.fullName)
        }

        findNavController().navigate(
            R.id.applicationChatFragment,
            bundle
        )
    }

    private fun observeApplications() {
        viewModel.applicationsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                EmployerApplicationsUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.contentLayout.isVisible = false
                }

                is EmployerApplicationsUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.contentLayout.isVisible = true

                    adapter.submitList(state.applications)
                    binding.emptyTextView.isVisible = state.applications.isEmpty()
                }

                is EmployerApplicationsUiState.Error -> {
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

                EmployerActionState.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is EmployerActionState.Success -> {
                    binding.progressBar.isVisible = false

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    viewModel.loadApplicationsByVacancy(vacancyId)
                }

                is EmployerActionState.Error -> {
                    binding.progressBar.isVisible = false

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showStatusMessageDialog(
        item: EmployerApplicationUiModel,
        status: ApplicationStatusDto
    ) {
        val application = item.application

        val editText = EditText(requireContext()).apply {
            hint = "Сообщение кандидату, необязательно"
            minLines = 3
            maxLines = 6
            setPadding(32, 24, 32, 24)
            setText(defaultMessageForStatus(status))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(statusTitle(status))
            .setMessage("Сообщение будет отправлено кандидату в чат.")
            .setView(editText)
            .setPositiveButton("Сохранить и отправить") { _, _ ->
                viewModel.updateApplicationStatus(
                    vacancyId = vacancyId,
                    applicationId = application.id,
                    status = status,
                    message = editText.text.toString()
                )
            }
            .setNegativeButton("Только статус") { _, _ ->
                viewModel.updateApplicationStatus(
                    vacancyId = vacancyId,
                    applicationId = application.id,
                    status = status,
                    message = null
                )
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun statusTitle(
        status: ApplicationStatusDto
    ): String {
        return when (status) {
            ApplicationStatusDto.VIEWED -> "Отметить как просмотренный"
            ApplicationStatusDto.INTERVIEW -> "Пригласить на интервью"
            ApplicationStatusDto.REJECTED -> "Отказать кандидату"
            ApplicationStatusDto.ACCEPTED -> "Принять кандидата"
            ApplicationStatusDto.NEW -> "Вернуть в новые"
        }
    }

    private fun defaultMessageForStatus(
        status: ApplicationStatusDto
    ): String {
        return when (status) {
            ApplicationStatusDto.VIEWED -> {
                "Здравствуйте! Мы просмотрели ваш отклик и скоро вернёмся с решением."
            }

            ApplicationStatusDto.INTERVIEW -> {
                "Здравствуйте! Ваш опыт нам интересен. Хотели бы пригласить вас на следующий этап."
            }

            ApplicationStatusDto.REJECTED -> {
                "Здравствуйте! Спасибо за отклик. Сейчас мы решили продолжить с другими кандидатами, но желаем вам успехов в поиске."
            }

            ApplicationStatusDto.ACCEPTED -> {
                "Здравствуйте! Мы готовы продолжить с вами процесс найма. Давайте обсудим следующие шаги."
            }

            ApplicationStatusDto.NEW -> ""
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}