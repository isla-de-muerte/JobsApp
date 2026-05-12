package com.example.jobsapp.presentation.employer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsapp.data.dto.ApplicationStatusDto
import com.example.jobsapp.databinding.ItemEmployerApplicationBinding

class EmployerApplicationsAdapter(
    private val onApplicationClick: (EmployerApplicationUiModel) -> Unit,
    private val onStatusClick: (EmployerApplicationUiModel, ApplicationStatusDto) -> Unit
) : RecyclerView.Adapter<EmployerApplicationsAdapter.EmployerApplicationViewHolder>() {

    private val items = mutableListOf<EmployerApplicationUiModel>()

    fun submitList(newItems: List<EmployerApplicationUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmployerApplicationViewHolder {
        val binding = ItemEmployerApplicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return EmployerApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: EmployerApplicationViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class EmployerApplicationViewHolder(
        private val binding: ItemEmployerApplicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EmployerApplicationUiModel) {
            val application = item.application
            val resume = application.resume

            binding.statusTextView.text = mapStatus(application.status)
            binding.fullNameTextView.text = resume.fullName
            binding.contactsTextView.text = resume.contacts
            binding.skillsTextView.text = "Навыки: ${resume.skills.joinToString(", ")}"

            binding.experienceTextView.text =
                "Опыт: ${resume.experience ?: "не указан"}"

            binding.educationTextView.text =
                "Образование: ${resume.education ?: "не указано"}"

            binding.portfolioTextView.text =
                "Портфолио: ${resume.portfolioUrl ?: "не указано"}"

            binding.coverLetterTextView.text =
                "Письмо: ${application.coverLetter ?: "не указано"}"

            binding.unreadBadgeTextView.isVisible = item.unreadCount > 0
            binding.unreadBadgeTextView.text = item.unreadCount.toString()

            binding.root.setOnClickListener {
                onApplicationClick(item)
            }

            binding.viewedButton.setOnClickListener {
                onStatusClick(item, ApplicationStatusDto.VIEWED)
            }

            binding.interviewButton.setOnClickListener {
                onStatusClick(item, ApplicationStatusDto.INTERVIEW)
            }

            binding.rejectButton.setOnClickListener {
                onStatusClick(item, ApplicationStatusDto.REJECTED)
            }

            binding.acceptButton.setOnClickListener {
                onStatusClick(item, ApplicationStatusDto.ACCEPTED)
            }

            val isFinal = application.status == "REJECTED" || application.status == "ACCEPTED"
            binding.actionsLayout.isVisible = !isFinal
        }

        private fun mapStatus(status: String): String {
            return when (status) {
                "NEW" -> "Новый"
                "VIEWED" -> "Просмотрен"
                "INTERVIEW" -> "Интервью"
                "REJECTED" -> "Отказ"
                "ACCEPTED" -> "Принят"
                else -> status
            }
        }
    }
}