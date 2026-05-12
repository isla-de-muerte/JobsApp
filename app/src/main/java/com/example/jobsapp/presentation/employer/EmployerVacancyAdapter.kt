package com.example.jobsapp.presentation.employer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.databinding.ItemEmployerVacancyBinding

class EmployerVacancyAdapter(
    private val onVacancyClick: (VacancyResponse) -> Unit,
    private val onPublishClick: (VacancyResponse) -> Unit,
    private val onResumeClick: (VacancyResponse) -> Unit,
    private val onArchiveClick: (VacancyResponse) -> Unit
) : RecyclerView.Adapter<EmployerVacancyAdapter.EmployerVacancyViewHolder>() {

    private val items = mutableListOf<VacancyResponse>()

    fun submitList(newItems: List<VacancyResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmployerVacancyViewHolder {
        val binding = ItemEmployerVacancyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return EmployerVacancyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: EmployerVacancyViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class EmployerVacancyViewHolder(
        private val binding: ItemEmployerVacancyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VacancyResponse) {
            val status = item.status.name
            val isDraft = status == "DRAFT"
            val isPublished = status == "PUBLISHED"
            val isPaused = status == "PAUSED_BY_OVERLOAD"
            val isArchived = status == "ARCHIVED"

            binding.titleTextView.text = item.title
            binding.salaryTextView.text = formatSalary(item)
            binding.formatTextView.text = mapWorkFormat(item.workFormat.name)
            binding.statusTextView.text = mapStatus(status)
            binding.viewsTextView.text = "Просмотры: ${item.viewsCount}"
            binding.applicationsTextView.text = "Отклики: ${item.applicationsCount}"

            binding.overloadHintTextView.isVisible = isPaused

            binding.publishButton.isVisible = isDraft || isPaused
            binding.archiveButton.isVisible = !isArchived

            binding.publishButton.text = when {
                isPaused -> "Открыть снова"
                isDraft -> "Опубликовать"
                else -> "Опубликовать"
            }

            binding.root.alpha = if (isArchived) {
                0.65f
            } else {
                1.0f
            }

            binding.root.setOnClickListener {
                onVacancyClick(item)
            }

            binding.publishButton.setOnClickListener {
                if (isPaused) {
                    onResumeClick(item)
                } else {
                    onPublishClick(item)
                }
            }

            binding.archiveButton.setOnClickListener {
                onArchiveClick(item)
            }
        }

        private fun formatSalary(item: VacancyResponse): String {
            val from = item.salaryFrom
            val to = item.salaryTo

            return when {
                from != null && to != null -> "$from - $to ₽"
                from != null -> "от $from ₽"
                to != null -> "до $to ₽"
                else -> "Зарплата не указана"
            }
        }

        private fun mapWorkFormat(value: String): String {
            return when (value) {
                "REMOTE" -> "Удалённо"
                "PART_TIME" -> "Подработка"
                "HYBRID" -> "Гибрид"
                "OFFICE" -> "Офис"
                else -> value
            }
        }

        private fun mapStatus(value: String): String {
            return when (value) {
                "DRAFT" -> "Черновик"
                "PUBLISHED" -> "Опубликована"
                "PAUSED_BY_OVERLOAD" -> "Отклики приостановлены"
                "ARCHIVED" -> "Архив"
                else -> value
            }
        }
    }
}