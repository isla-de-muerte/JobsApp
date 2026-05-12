package com.example.jobsapp.presentation.chat

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
import com.example.jobsapp.databinding.FragmentApplicationChatBinding
import com.example.jobsapp.di.AppModule

class ApplicationChatFragment : Fragment() {

    private var _binding: FragmentApplicationChatBinding? = null
    private val binding: FragmentApplicationChatBinding
        get() = requireNotNull(_binding)

    private val viewModel: ApplicationChatViewModel by viewModels {
        AppModule.provideApplicationChatViewModelFactory(requireContext())
    }

    private val adapter = ApplicationMessageAdapter()

    private lateinit var applicationId: String
    private lateinit var title: String

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        applicationId = requireArguments().getString("applicationId")
            ?: error("applicationId is required")

        title = requireArguments().getString("title")
            ?: "Чат по отклику"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicationChatBinding.inflate(
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

        binding.titleTextView.text = title

        setupRecyclerView()
        setupListeners()
        observeState()

        viewModel.loadMessages(applicationId)
    }

    private fun setupRecyclerView() {
        binding.messagesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.messagesRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.backTextView.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.sendButton.setOnClickListener {
            sendCurrentMessage()
        }

        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendCurrentMessage() {
        val message = binding.messageEditText.text
            .toString()
            .trim()

        if (message.isBlank()) {
            return
        }

        binding.sendButton.isEnabled = false

        viewModel.sendMessage(
            applicationId = applicationId,
            message = message
        )

        binding.messageEditText.text.clear()
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ApplicationChatUiState.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is ApplicationChatUiState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.sendButton.isEnabled = true

                    adapter.submitList(state.messages)

                    if (state.messages.isNotEmpty()) {
                        binding.messagesRecyclerView.scrollToPosition(
                            state.messages.lastIndex
                        )
                    }
                }

                is ApplicationChatUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.sendButton.isEnabled = true

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