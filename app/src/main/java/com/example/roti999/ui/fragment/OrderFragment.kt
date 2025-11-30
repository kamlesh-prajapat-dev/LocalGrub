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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.roti999.ui.adapter.OrderSummaryAdapter
import com.example.roti999.databinding.FragmentOrderBinding
import com.example.roti999.ui.viewmodel.OrderViewModel
import com.example.roti999.ui.viewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalPrice.collect {
                binding.totalPriceTextView.text = "Rs. $it"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orderUIState.collect { state ->
                // Here you would handle UI changes for loading, success, error
                when(state) {
                    is OrderViewModel.OrderUIState.Success -> {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Order Placed", Toast.LENGTH_SHORT).show()
                        // Optionally navigate away
                        // findNavController().navigate(...)
                        sharedHCOViewModel.clearSelectItemList()
                        val action = OrderFragmentDirections.actionOrderFragmentToHomeFragment()
                        findNavController().navigate(action)
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
    }
    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedHCOViewModel.user.collect { user ->
                    if (user != null) {
                        binding.nameTextView.text = user.name
                        binding.phoneNumberTextView.text = user.phoneNumber
                        binding.addressTextView.text = user.address
                        viewModel.updateUserData(user)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sharedHCOViewModel.selectItemList.collect { items ->
                orderSummaryAdapter.submitList(items)
                viewModel.updateOrderDetails(items)
            }
        }
    }
    private fun setupClickListeners() {
        binding.placeOrderButton.setOnClickListener {
            viewModel.placeOrder()
        }
        binding.editProfileButton.setOnClickListener {
            // Future navigation to profile screen
            val action = OrderFragmentDirections.actionOrderFragmentToCreateYourProfileFragment()
            findNavController().navigate(action)
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
