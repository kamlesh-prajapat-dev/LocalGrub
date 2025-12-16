package com.example.roti999.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roti999.R
import com.example.roti999.data.dto.Order
import com.example.roti999.databinding.OrderHistoryItemBinding
import com.example.roti999.util.Constant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderHistoryItemAdapter(private val listener: OrderHistoryItemClickListener) : ListAdapter<Order, OrderHistoryItemAdapter.OrderHistoryItemViewHolder>(OrderHistoryItemDiffCallback()) {
    interface OrderHistoryItemClickListener {
        fun onViewDetailsOrderHistoryItem(item: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryItemViewHolder {
        val binding = OrderHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderHistoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderHistoryItemViewHolder(private val binding: OrderHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Order) {
            val timestamp = item.placeAt     // Firestore Timestamp
            val localDate = timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            binding.orderNameTextView.text = item.id
            binding.orderDateTextView.text = localDate.format(formatter)
            binding.totalPriceTextView.text = "Rs. ${item.totalPrice}"
            binding.orderStatusTextView.text = item.status

            if (item.status == Constant.DELIVERED.name) {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.green_status_background)
            } else {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
            }

            binding.viewDetailsButton.setOnClickListener { listener.onViewDetailsOrderHistoryItem(item = item) }
        }
    }

    class OrderHistoryItemDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
