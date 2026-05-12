package com.example.jobsapp.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jobsapp.R
import com.example.jobsapp.databinding.FragmentSplashBinding
import com.example.jobsapp.di.AppModule

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding: FragmentSplashBinding
        get() = requireNotNull(_binding)

    private val viewModel: SplashViewModel by viewModels {
        AppModule.provideSplashViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(
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

        binding.root.postDelayed(
            {
                when (viewModel.resolveDestination()) {
                    SplashDestination.Login -> {
                        findNavController().navigate(
                            R.id.action_splashFragment_to_loginFragment
                        )
                    }

                    SplashDestination.ApplicantHome -> {
                        findNavController().navigate(
                            R.id.action_splashFragment_to_vacancyListFragment
                        )
                    }

                    SplashDestination.EmployerHome -> {
                        findNavController().navigate(
                            R.id.action_splashFragment_to_employerVacanciesFragment
                        )
                    }
                }
            },
            700L
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}