package com.example.jobsapp.presentation.favorites

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
import com.example.jobsapp.databinding.FragmentFavoritesBinding
import com.example.jobsapp.di.AppModule
import com.example.jobsapp.presentation.vacancies.VacancyAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = requireNotNull(_binding)

    private val viewModel: FavoritesViewModel by viewModels {
        AppModule.provideFavoritesViewModelFactory(requireContext())
    }

    private val adapter = VacancyAdapter(
        onClick = { vacancy ->
            val bundle = Bundle().apply {
                putString("vacancyId", vacancy.id)
            }

            findNavController().navigate(
                R.id.action_favoritesFragment_to_vacancyDetailsFragment,
                bundle
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(
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

        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        binding.favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.favoritesRecyclerView.adapter = adapter
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                FavoritesUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.contentLayout.isVisible = false
                }

                is FavoritesUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.contentLayout.isVisible = true

                    adapter.submitList(state.vacancies)
                    binding.emptyTextView.isVisible = state.vacancies.isEmpty()
                }

                is FavoritesUiState.Error -> {
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