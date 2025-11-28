package com.example.roti999.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roti999.R
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.databinding.OrderItemSummaryRowBinding

class OrderSummaryAdapter : ListAdapter<FoodItem, OrderSummaryAdapter.OrderItemViewHolder>(FoodItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding =
            OrderItemSummaryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderItemViewHolder(private val binding: OrderItemSummaryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("InflateParams", "SetTextI18n")
        fun bind(item: FoodItem) {
            binding.itemNameAndQuantityTextView.text = "${item.name} (x${item.quantity})"
            binding.itemPriceTextView.text = "Rs. ${item.price * item.quantity}"

            binding.addOnsContainer.removeAllViews() // Clear previous add-ons

            for (addOn in item.addOns) {
                val addOnView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.add_on_item_row, binding.addOnsContainer, false)
                val addOnNameTextView = addOnView.findViewById<TextView>(R.id.addOnNameTextView)
                val addOnPriceTextView = addOnView.findViewById<TextView>(R.id.addOnPriceTextView)

                addOnNameTextView.text = "- ${addOn.name}"
                addOnPriceTextView.text = "Rs. ${addOn.price}"

                binding.addOnsContainer.addView(addOnView)
            }
        }
    }

    class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
