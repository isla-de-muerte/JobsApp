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
import com.example.jobsapp.R
import com.example.jobsapp.data.dto.EmployerProfileResponse
import com.example.jobsapp.databinding.FragmentEmployerProfileBinding
import com.example.jobsapp.di.AppModule

class EmployerProfileFragment : Fragment() {

    private var _binding: FragmentEmployerProfileBinding? = null
    private val binding: FragmentEmployerProfileBinding
        get() = requireNotNull(_binding)

    private val viewModel: EmployerProfileViewModel by viewModels {
        AppModule.provideEmployerProfileViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerProfileBinding.inflate(
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
        observeState()

        viewModel.loadProfile()
    }

    private fun setupListeners() {
        binding.backTextView.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.saveButton.setOnClickListener {
            viewModel.saveProfile(
                companyName = binding.companyNameEditText.text.toString(),
                description = binding.descriptionEditText.text.toString(),
                website = binding.websiteEditText.text.toString()
            )
        }

        binding.logoutButton.setOnClickListener {
            AppModule.provideTokenStorage(requireContext()).clearTokens()

            findNavController().navigate(
                R.id.loginFragment
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                EmployerProfileUiState.Idle -> {
                    setLoading(false)
                }

                EmployerProfileUiState.Loading -> {
                    setLoading(true)
                }

                is EmployerProfileUiState.Loaded -> {
                    setLoading(false)
                    renderProfile(state.profile)
                }

                is EmployerProfileUiState.Saved -> {
                    setLoading(false)
                    renderProfile(state.profile)

                    Toast.makeText(
                        requireContext(),
                        "Профиль компании сохранён",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is EmployerProfileUiState.Error -> {
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

    private fun renderProfile(profile: EmployerProfileResponse) {
        binding.companyNameEditText.setText(profile.companyName)
        binding.descriptionEditText.setText(profile.description.orEmpty())
        binding.websiteEditText.setText(profile.website.orEmpty())
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.contentScrollView.isVisible = !isLoading
        binding.saveButton.isEnabled = !isLoading
        binding.logoutButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}