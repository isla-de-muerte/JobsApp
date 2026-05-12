package com.example.jobsapp.presentation.employer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jobsapp.R
import com.example.jobsapp.data.dto.CategoryResponse
import com.example.jobsapp.data.dto.WorkFormatDto
import com.example.jobsapp.databinding.FragmentCreateVacancyBinding
import com.example.jobsapp.di.AppModule

class CreateVacancyFragment : Fragment() {

    private var _binding: FragmentCreateVacancyBinding? = null
    private val binding: FragmentCreateVacancyBinding
        get() = requireNotNull(_binding)

    private val viewModel: EmployerViewModel by viewModels {
        AppModule.provideEmployerViewModelFactory(requireContext())
    }

    private var categories: List<CategoryResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVacancyBinding.inflate(
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
        observeCategories()
        observeActions()

        viewModel.loadCategories()
    }

    private fun setupListeners() {
        binding.backTextView.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.createButton.setOnClickListener {
            val selectedCategory = categories.getOrNull(
                binding.categorySpinner.selectedItemPosition
            )

            viewModel.createVacancy(
                categoryId = selectedCategory?.id.orEmpty(),
                title = binding.titleEditText.text.toString(),
                description = binding.descriptionEditText.text.toString(),
                requirements = binding.requirementsEditText.text.toString(),
                salaryFrom = binding.salaryFromEditText.text.toString(),
                salaryTo = binding.salaryToEditText.text.toString(),
                workFormat = selectedWorkFormat()
            )
        }
    }

    private fun observeCategories() {
        viewModel.categoriesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CategoriesUiState.Loading -> {
                    setLoading(true)
                }

                is CategoriesUiState.Success -> {
                    setLoading(false)
                    categories = state.categories
                    setupCategorySpinner(state.categories)
                }

                is CategoriesUiState.Error -> {
                    setLoading(false)

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupCategorySpinner(
        categories: List<CategoryResponse>
    ) {
        val names = categories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        binding.categorySpinner.adapter = adapter
    }

    private fun observeActions() {
        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                EmployerActionState.Idle -> {
                    setLoading(false)
                }

                EmployerActionState.Loading -> {
                    setLoading(true)
                }

                is EmployerActionState.Success -> {
                    setLoading(false)

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()
                }

                is EmployerActionState.Error -> {
                    setLoading(false)

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun selectedWorkFormat(): WorkFormatDto {
        return when (binding.workFormatRadioGroup.checkedRadioButtonId) {
            R.id.hybridRadioButton -> WorkFormatDto.HYBRID
            R.id.officeRadioButton -> WorkFormatDto.OFFICE
            R.id.partTimeRadioButton -> WorkFormatDto.PART_TIME
            else -> WorkFormatDto.REMOTE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.contentScrollView.isVisible = !isLoading
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}