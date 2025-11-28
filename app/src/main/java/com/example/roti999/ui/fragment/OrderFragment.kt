package com.example.roti999.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.roti999.adapter.OrderSummaryAdapter
import com.example.roti999.databinding.FragmentOrderBinding
import com.example.roti999.ui.viewmodel.OrderViewModel
import com.example.roti999.ui.viewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var orderSummaryAdapter: OrderSummaryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLoading(true)
        onSetRecyclerView()
        observeSharedViewModel()
        observeViewModel()
        setupClickListeners()
        setLoading(false)
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.totalPrice.observe(viewLifecycleOwner) {
            binding.totalPriceTextView.text = "Rs. $it"
        }

        viewModel.orderUIState.observe(viewLifecycleOwner) { state ->
            // Here you would handle UI changes for loading, success, error
            when(state) {
                is OrderViewModel.OrderUIState.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Order Placed!", Toast.LENGTH_SHORT).show()
                    // Optionally navigate away
                    // findNavController().navigate(...)
                    sharedHCOViewModel.clearSelectItemList()
                    findNavController().navigateUp()
                }
                is OrderViewModel.OrderUIState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is OrderViewModel.OrderUIState.Loading -> {
                    // Handle Idle and Loading states, e.g., show a progress bar
                    setLoading(true)
                }
                is OrderViewModel.OrderUIState.Idle -> {
                    setLoading(false)
                }
            }
        }
    }
    private fun observeSharedViewModel() {
        sharedHCOViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.nameEditText.setText(user.name)
                binding.phoneNumberEditText.setText(user.phoneNumber)
                binding.addressEditText.setText(user.address)
                viewModel.updateUserData(user)
            }
        }

        sharedHCOViewModel.selectItemList.observe(viewLifecycleOwner) {items ->
            orderSummaryAdapter.submitList(items)
            viewModel.updateOrderDetails(items)
        }
    }

    private fun setupClickListeners() {
        binding.placeOrderButton.setOnClickListener {
            viewModel.createOrder()
        }
        binding.editProfileButton.setOnClickListener {
            // Future navigation to profile screen
            val name = binding.nameEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            viewModel.editUser(name = name, address = address)
        }
    }

    private fun onSetRecyclerView() {
        orderSummaryAdapter = OrderSummaryAdapter()
        binding.orderItemsRecyclerView.adapter = orderSummaryAdapter
    }

    private fun setLoading(isLoading: Boolean) {
        // You would show/hide a progress bar here
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
