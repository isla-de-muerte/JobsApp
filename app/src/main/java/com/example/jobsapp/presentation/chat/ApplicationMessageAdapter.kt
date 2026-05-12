package com.example.jobsapp.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jobsapp.data.dto.ApplicationMessageResponse
import com.example.jobsapp.databinding.ItemApplicationMessageBinding

class ApplicationMessageAdapter : RecyclerView.Adapter<ApplicationMessageAdapter.MessageViewHolder>() {

    private val items = mutableListOf<ApplicationMessageResponse>()

    fun submitList(newItems: List<ApplicationMessageResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val binding = ItemApplicationMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MessageViewHolder(
        private val binding: ItemApplicationMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ApplicationMessageResponse) {
            binding.senderTextView.text = when (item.senderRole) {
                "EMPLOYER" -> "Работодатель"
                "APPLICANT" -> "Соискатель"
                else -> item.senderRole
            }

            binding.messageTextView.text = item.message
            binding.dateTextView.text = item.createdAt
        }
    }
}