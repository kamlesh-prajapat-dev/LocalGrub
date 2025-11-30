package com.example.roti999.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentEachOrderStatusBinding
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.ui.adapter.OrderSummaryAdapter
import com.example.roti999.ui.viewmodel.EachOrderStatusViewModel
import com.example.roti999.ui.viewmodel.SharedHFToEOSFViewModel
import com.example.roti999.util.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EachOrderStatusFragment : Fragment() {
    private var _binding: FragmentEachOrderStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EachOrderStatusViewModel by viewModels()
    private lateinit var orderSummaryAdapter: OrderSummaryAdapter

    private val sharedHFToEOSFViewModel: SharedHFToEOSFViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEachOrderStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onSetRecyclerView()
        observeSharedViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onSetRecyclerView() {
        orderSummaryAdapter = OrderSummaryAdapter()
        binding.orderedItemsRecyclerView.adapter = orderSummaryAdapter
    }

    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedHFToEOSFViewModel.order.collect { order ->
                if (order != null) {
                    when(order.status) {
                        Constant.PLACED.name -> {
                            setPlaced()
                        }
                        Constant.CONFIRMED.name -> {
                            setConfirmed()
                        }
                        Constant.PREPARING.name -> {
                            setPreparing()
                        }
                        Constant.OUT_FOR_DELIVERY.name -> {
                            setOutForDelivery()
                        }
                        Constant.DELIVERED.name -> {
                            setDelivered()
                        }
                    }
                    val selectedItems = order.items
                    val items = selectedItems.map { dish ->
                        FoodItem(
                            id = dish.id,
                            name = dish.name,
                            description = dish.description,
                            price = dish.price,
                            imageUrl = dish.thumbnail, // map thumbnail → imageUrl
                            quantity = dish.quantity,
                            isSelected = true
                        )
                    }
                    orderSummaryAdapter.submitList(items)

                    binding.addressTextView.text = order.userAddress

                    // Populate other UI fields from the order object
                    binding.itemTotalTextView.text = "Rs. ${order.totalPrice}"

                    // You can replace this with your actual delivery fee logic
                    val deliveryFee = 0.0
                    binding.deliveryFeeTextView.text = "Rs. $deliveryFee"
                    binding.grandTotalTextView.text = "Rs. ${order.totalPrice + deliveryFee}"

                    viewModel.onSetSelectItemList(items)
                    viewModel.onSetOrder(order)
                }
            }
        }
    }

    private fun setPlaced() {
        binding.step1Icon.setImageResource(R.drawable.stepper_background_complete)
        binding.step1Title.text = "ORDER ${Constant.PLACED.name}"
        binding.step1Title.setTypeface(binding.step1Title.typeface, Typeface.BOLD)
        binding.step1Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        binding.step1Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

        resetConfirmed()
        resetPreparing()
        resetOutForDelivery()
        resetDelivered()
    }

    private fun setConfirmed() {
        setPlaced()

        binding.step2Icon.setImageResource(R.drawable.stepper_background_complete)
        binding.step2Title.text = "ORDER ${Constant.CONFIRMED.name}"
        binding.step2Title.setTypeface(binding.step1Title.typeface, Typeface.BOLD)
        binding.step2Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        binding.step2Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

        resetPreparing()
        resetOutForDelivery()
        resetDelivered()
    }

    private fun resetConfirmed() {
        binding.step2Icon.setImageResource(R.drawable.stepper_background_incomplete)
        binding.step2Title.text = "ORDER ${Constant.CONFIRMED.name}"
        binding.step2Title.setTypeface(null, Typeface.NORMAL)
        binding.step2Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        binding.step2Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
    }

    private fun setPreparing() {
        setPlaced()
        setConfirmed()

        binding.step3Icon.setImageResource(R.drawable.stepper_background_complete)
        binding.step3Title.text = "ORDER ${Constant.PREPARING.name}"
        binding.step3Title.setTypeface(binding.step1Title.typeface, Typeface.BOLD)
        binding.step3Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        binding.step3Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

        resetOutForDelivery()
        resetDelivered()
    }

    private fun resetPreparing() {
        binding.step3Icon.setImageResource(R.drawable.stepper_background_incomplete)
        binding.step3Title.text = "ORDER ${Constant.PREPARING.name}"
        binding.step3Title.setTypeface(null, Typeface.NORMAL)
        binding.step3Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        binding.step3Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
    }

    private fun setOutForDelivery() {
        setPlaced()
        setConfirmed()
        setPreparing()

        binding.step4Icon.setImageResource(R.drawable.stepper_background_complete)
        binding.step4Title.text = "ORDER ${Constant.OUT_FOR_DELIVERY.name}"
        binding.step4Title.setTypeface(binding.step1Title.typeface, Typeface.BOLD)
        binding.step4Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        binding.step4Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

        resetDelivered()
    }

    private fun resetOutForDelivery() {
        binding.step4Icon.setImageResource(R.drawable.stepper_background_incomplete)
        binding.step4Title.text = "ORDER ${Constant.OUT_FOR_DELIVERY.name}"
        binding.step4Title.setTypeface(null, Typeface.NORMAL)
        binding.step4Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        binding.step4Line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
    }

    private fun setDelivered() {
        setPlaced()
        setConfirmed()
        setPreparing()
        setOutForDelivery()

        binding.step5Icon.setImageResource(R.drawable.stepper_background_complete)
        binding.step5Title.text = "ORDER ${Constant.DELIVERED.name}"
        binding.step5Title.setTypeface(binding.step5Title.typeface, Typeface.BOLD)
        binding.step5Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
    }

    private fun resetDelivered() {
        binding.step5Icon.setImageResource(R.drawable.stepper_background_incomplete)
        binding.step5Title.text = "ORDER ${Constant.DELIVERED.name}"
        binding.step5Title.setTypeface(null, Typeface.NORMAL)
        binding.step5Title.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
    }
}