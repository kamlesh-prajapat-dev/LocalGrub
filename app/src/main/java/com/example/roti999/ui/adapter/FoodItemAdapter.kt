package com.example.roti999.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.roti999.R
import com.example.roti999.domain.model.AddOn
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.databinding.FoodItemCardBinding

class FoodItemAdapter(private val listener: FoodItemClickListener) : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    interface FoodItemClickListener {
        fun onIncreaseQuantity(item: FoodItem)
        fun onDecreaseQuantity(item: FoodItem)
        fun onSelectItem(item: FoodItem, isSelected: Boolean)
        fun onAddOnSelected(item: FoodItem, addOn: AddOn, isSelected: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val binding = FoodItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodItemViewHolder(private val binding: FoodItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: FoodItem) {
            binding.dishNameTextView.text = item.name
            binding.dishDescriptionTextView.text = item.description
            binding.priceTextView.text = "Rs. ${item.price}"
            binding.quantityTextView.text = item.quantity.toString()
            binding.selectItemCheckBox.isChecked = item.isSelected

            val rawImageUrl = item.imageUrl.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")

            Glide.with(binding.root.context)
                .load(rawImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.dishImageView)

            binding.increaseQuantityButton.setOnClickListener { listener.onIncreaseQuantity(item) }
            binding.decreaseQuantityButton.setOnClickListener { listener.onDecreaseQuantity(item) }
            binding.selectItemCheckBox.setOnCheckedChangeListener { _, isChecked -> listener.onSelectItem(item, isChecked) }

            if (item.addOns.isNotEmpty()) {
                binding.addOnsLabel.visibility = View.VISIBLE
                binding.firstAddOns.visibility = View.VISIBLE
                binding.firstAddOns.text = "${item.addOns[0].name} (+Rs. ${item.addOns[0].price})"
                binding.firstAddOns.isChecked = item.addOns[0].isSelected
                binding.firstAddOns.setOnCheckedChangeListener { _, isChecked -> listener.onAddOnSelected(item, item.addOns[0], isChecked) }

                if (item.addOns.size > 1) {
                    binding.secondAddOns.visibility = View.VISIBLE
                    binding.secondAddOns.text = "${item.addOns[1].name} (+Rs. ${item.addOns[1].price})"
                    binding.secondAddOns.isChecked = item.addOns[1].isSelected
                    binding.secondAddOns.setOnCheckedChangeListener { _, isChecked -> listener.onAddOnSelected(item, item.addOns[1], isChecked) }
                }
            } else {
                binding.addOnsLabel.visibility = View.GONE
                binding.firstAddOns.visibility = View.GONE
                binding.secondAddOns.visibility = View.GONE
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
