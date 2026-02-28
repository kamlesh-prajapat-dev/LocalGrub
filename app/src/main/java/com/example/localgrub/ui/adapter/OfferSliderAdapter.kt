package com.example.localgrub.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localgrub.R
import com.example.localgrub.data.model.GetOffer
import com.example.localgrub.databinding.ItemOfferCardBinding

class OfferSliderAdapter : ListAdapter<GetOffer, OfferSliderAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemOfferCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(private val binding: ItemOfferCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: GetOffer) {
            with(binding) {
                offerTitle.text = offer.promoCode
                offerSubtitle.text = offer.description

                if (offer.bannerImageUrl.isEmpty()) {
                    offerBannerImage.visibility = View.GONE
                    imageOverlay.visibility = View.GONE
                } else {
                    offerBannerImage.visibility = View.VISIBLE
                    imageOverlay.visibility = View.VISIBLE
                    Glide.with(offerBannerImage.context)
                        .load(offer.bannerImageUrl)
                        .placeholder(R.drawable.default_offer_background)
                        .error(R.drawable.default_offer_background)
                        .into(offerBannerImage)
                }
            }
        }
    }

    class OfferDiffCallback : DiffUtil.ItemCallback<GetOffer>() {
        override fun areItemsTheSame(oldItem: GetOffer, newItem: GetOffer): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GetOffer, newItem: GetOffer): Boolean = oldItem == newItem
    }
}
