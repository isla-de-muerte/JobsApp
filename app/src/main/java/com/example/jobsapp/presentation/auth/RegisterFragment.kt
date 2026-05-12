package com.example.jobsapp.presentation.auth

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
import com.example.jobsapp.data.dto.UserRoleDto
import com.example.jobsapp.databinding.FragmentRegisterBinding
import com.example.jobsapp.di.AppModule

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding
        get() = requireNotNull(_binding)

    private val viewModel: AuthViewModel by viewModels {
        AppModule.provideAuthViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(
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
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val role = if (binding.employerRadioButton.isChecked) {
                UserRoleDto.EMPLOYER
            } else {
                UserRoleDto.APPLICANT
            }

            viewModel.register(
                email = binding.emailEditText.text.toString(),
                password = binding.passwordEditText.text.toString(),
                role = role
            )
        }

        binding.loginTextView.setOnClickListener {
            findNavController().navigate(
                R.id.action_registerFragment_to_loginFragment
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthUiState.Idle -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true
                }

                AuthUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.registerButton.isEnabled = false
                }

                is AuthUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true

                    val actionId = when (state.role) {
                        com.example.jobsapp.data.dto.UserRoleDto.APPLICANT ->
                            R.id.action_registerFragment_to_vacancyListFragment

                        com.example.jobsapp.data.dto.UserRoleDto.EMPLOYER ->
                            R.id.action_registerFragment_to_employerVacanciesFragment
                    }

                    findNavController().navigate(actionId)
                }

                is AuthUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true

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