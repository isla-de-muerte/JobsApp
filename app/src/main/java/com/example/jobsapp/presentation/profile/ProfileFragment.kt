package com.example.jobsapp.presentation.profile

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
import com.example.jobsapp.data.dto.ApplicantProfileResponse
import com.example.jobsapp.databinding.FragmentProfileBinding
import com.example.jobsapp.di.AppModule

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = requireNotNull(_binding)

    private val viewModel: ProfileViewModel by viewModels {
        AppModule.provideProfileViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(
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
        binding.saveButton.setOnClickListener {
            viewModel.saveProfile(
                fullName = binding.fullNameEditText.text.toString(),
                contacts = binding.contactsEditText.text.toString(),
                skillsRaw = binding.skillsEditText.text.toString(),
                experience = binding.experienceEditText.text.toString(),
                education = binding.educationEditText.text.toString(),
                portfolioUrl = binding.portfolioEditText.text.toString()
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
                ProfileUiState.Idle -> {
                    setLoading(false)
                }

                ProfileUiState.Loading -> {
                    setLoading(true)
                }

                is ProfileUiState.Loaded -> {
                    setLoading(false)
                    renderProfile(state.profile)
                }

                is ProfileUiState.Saved -> {
                    setLoading(false)
                    renderProfile(state.profile)

                    Toast.makeText(
                        requireContext(),
                        "Резюме сохранено",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ProfileUiState.Error -> {
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

    private fun renderProfile(profile: ApplicantProfileResponse) {
        binding.fullNameEditText.setText(profile.fullName)
        binding.contactsEditText.setText(profile.contacts)
        binding.skillsEditText.setText(profile.skills.joinToString(", "))
        binding.experienceEditText.setText(profile.experience.orEmpty())
        binding.educationEditText.setText(profile.education.orEmpty())
        binding.portfolioEditText.setText(profile.portfolioUrl.orEmpty())
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.saveButton.isEnabled = !isLoading
        binding.logoutButton.isEnabled = !isLoading
        binding.contentScrollView.isVisible = !isLoading
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}