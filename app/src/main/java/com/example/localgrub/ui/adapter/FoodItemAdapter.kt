package com.example.localgrub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localgrub.R
import com.example.localgrub.data.model.firebase.FoodItem
import com.example.localgrub.databinding.FoodItemCardBinding

class FoodItemAdapter(private val listener: FoodItemClickListener) :
    ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    interface FoodItemClickListener {
        fun onIncreaseQuantity(item: FoodItem)
        fun onDecreaseQuantity(item: FoodItem)
        fun onSelectItem(item: FoodItem, isSelected: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val binding = FoodItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodItemViewHolder(private val binding: FoodItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.addBtn.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    listener.onSelectItem(item, !item.isSelected)
                }
            }

            binding.increaseQuantityButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onIncreaseQuantity(getItem(position))
                }
            }

            binding.decreaseQuantityButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDecreaseQuantity(getItem(position))
                }
            }
        }

        fun bind(item: FoodItem) {
            val context = binding.root.context
            
            with(binding) {
                dishNameTextView.text = item.name
                dishDescriptionTextView.text = item.description
                priceTextView.text = context.getString(R.string.price_format, item.price)

                val rawImageUrl = processImageUrl(item.imageUrl)

                Glide.with(dishImageView.context)
                    .load(rawImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(dishImageView)

                if (item.available) {
                    stockTxt.isVisible = false
                    if (!item.isSelected) {
                        addBtn.isVisible = true
                        addBtn.text = context.getString(R.string.add_btn_text)
                        addBtn.setIconResource(R.drawable.ic_add)
                        addBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                        quantityControlsLayout.isVisible = false
                    } else {
                        addBtn.isVisible = false
                        quantityControlsLayout.isVisible = true
                        quantityTextView.text = context.getString(R.string.quantity_format, item.quantity)
                    }
                } else {
                    addBtn.isVisible = false
                    stockTxt.isVisible = true
                    stockTxt.text = context.getString(R.string.out_of_stock)
                    quantityControlsLayout.isVisible = false
                }
            }
        }

        private fun processImageUrl(url: String): String {
            return url.replace("github.com", "raw.githubusercontent.com")
                .replace("/blob/", "/")
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
