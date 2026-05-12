package com.example.jobsapp.presentation.applications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsapp.databinding.ItemApplicationBinding

class ApplicationsAdapter(
    private val onClick: (ApplicationUiModel) -> Unit
) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationViewHolder>() {

    private val items = mutableListOf<ApplicationUiModel>()

    fun submitList(newItems: List<ApplicationUiModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationViewHolder {
        val binding = ItemApplicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ApplicationViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ApplicationViewHolder(
        private val binding: ItemApplicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ApplicationUiModel) {
            val application = item.application

            binding.statusTextView.text = mapStatus(application.status)
            binding.vacancyIdTextView.text = item.vacancyTitle

            binding.coverLetterTextView.text =
                application.coverLetter ?: "Сопроводительное письмо не указано"

            binding.dateTextView.text = "Дата отклика: ${application.createdAt}"

            binding.unreadBadgeTextView.isVisible = item.unreadCount > 0
            binding.unreadBadgeTextView.text = item.unreadCount.toString()

            binding.root.setOnClickListener {
                onClick(item)
            }
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