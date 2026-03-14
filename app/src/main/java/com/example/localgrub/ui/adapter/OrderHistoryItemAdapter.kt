package com.example.localgrub.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localgrub.R
import com.example.localgrub.data.model.firebase.FetchedOrder
import com.example.localgrub.databinding.OrderHistoryItemBinding
import com.example.localgrub.util.OrderStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class OrderHistoryItemAdapter(private val listener: OrderHistoryItemClickListener) :
    ListAdapter<FetchedOrder, OrderHistoryItemAdapter.OrderHistoryItemViewHolder>(
        OrderHistoryItemDiffCallback()
    ) {
    interface OrderHistoryItemClickListener {
        fun onViewDetailsOrderHistoryItem(item: FetchedOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryItemViewHolder {
        val binding =
            OrderHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderHistoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderHistoryItemViewHolder(private val binding: OrderHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: FetchedOrder) {
            val timestamp = item.placeAt     // Firestore Timestamp
            val localDate = Date(timestamp)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            binding.orderNameTextView.text = item.id
            binding.orderDateTextView.text = localDate.format(formatter)
            binding.totalPriceTextView.text = "Rs. ${item.totalPrice}"
            binding.orderStatusTextView.text = item.status

            when (item.status) {
                OrderStatus.DELIVERED -> {
                    binding.orderStatusTextView.setBackgroundResource(R.drawable.green_status_background)
                }

                OrderStatus.CANCELLED -> {
                    binding.orderStatusTextView.setBackgroundResource(R.drawable.red_status_background)
                }

                else -> {
                    binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
                }
            }

            binding.viewDetailsButton.setOnClickListener {
                listener.onViewDetailsOrderHistoryItem(
                    item = item
                )
            }
        }
    }

    class OrderHistoryItemDiffCallback : DiffUtil.ItemCallback<FetchedOrder>() {
        override fun areItemsTheSame(oldItem: FetchedOrder, newItem: FetchedOrder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FetchedOrder, newItem: FetchedOrder): Boolean {
            return oldItem == newItem
        }
    }
}
