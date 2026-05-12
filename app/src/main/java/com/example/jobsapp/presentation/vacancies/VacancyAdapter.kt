package com.example.jobsapp.presentation.vacancies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsapp.data.dto.VacancyResponse
import com.example.jobsapp.databinding.ItemVacancyBinding

class VacancyAdapter(
    private val onClick: (VacancyResponse) -> Unit
) : RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder>() {

    private val items = mutableListOf<VacancyResponse>()

    fun submitList(newItems: List<VacancyResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VacancyViewHolder {
        val binding = ItemVacancyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return VacancyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VacancyViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VacancyViewHolder(
        private val binding: ItemVacancyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VacancyResponse) {
            binding.titleTextView.text = item.title
            binding.descriptionTextView.text = item.description
            binding.workFormatTextView.text = mapWorkFormat(item.workFormat.name)
            binding.salaryTextView.text = formatSalary(item)

            binding.root.setOnClickListener {
                onClick(item)
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
    }
}