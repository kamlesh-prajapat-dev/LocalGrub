package com.example.roti999.ui.screens.order

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.example.roti999.R
import com.example.roti999.data.model.SelectedDish
import com.example.roti999.ui.adapter.OrderSummaryAdapter
import com.example.roti999.databinding.FragmentOrderBinding
import com.example.roti999.ui.sharedviewmodel.SharedHCOViewModel
import com.example.roti999.ui.sharedviewmodel.SharedHFToEOSFViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderFragment : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private val sharedHFToEOSFViewModel: SharedHFToEOSFViewModel by activityViewModels()
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

        onSetLoading(true)
        onSetRecyclerView()
        observeSharedViewModel()
        observeViewModel()
        setupClickListeners()
        onSetLoading(false)
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalPrice.collect {
                    binding.totalPriceTextView.text = "Rs. $it"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderUIState.collect { state ->
                    // Here you would handle UI changes for loading, success, error
                    handleUIState(state)
                }
            }
        }
    }
    private fun handleUIState(state: OrderUIState) {
        when (state) {
            is OrderUIState.Success -> {
                sharedHFToEOSFViewModel.onSetOrder(state.order)
                sharedHCOViewModel.clearSelectItemList()
                Toast.makeText(requireContext(), "Order Placed", Toast.LENGTH_SHORT).show()
                val action = OrderFragmentDirections.actionOrderFragmentToEachOrderStatusFragment()
                findNavController().navigate(action)
                onSetLoading(false)
            }

            is OrderUIState.Error -> {
                onSetLoading(false)
                Toast.makeText(requireContext(), state.e.message, Toast.LENGTH_SHORT).show()
            }

            is OrderUIState.Loading -> {
                // Handle Idle and Loading states, e.g., show a progress bar
                onSetLoading(true)
            }

            is OrderUIState.Idle -> {
                onSetLoading(false)
            }

            is OrderUIState.ValidationError -> {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                onSetLoading(false)
            }

            is OrderUIState.NoInternet -> {
                showNoInternetDialog()
                onSetLoading(false)
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedHCOViewModel.selectItemList.collect { items ->
                    val itemList = items.map {
                        SelectedDish(
                            id = it.id,
                            price = it.price,
                            quantity = it.quantity,
                            name = it.name
                        )
                    }
                    orderSummaryAdapter.submitList(itemList)
                    viewModel.updateOrderDetails(items)
                }
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
    private fun onSetLoading(isLoading: Boolean) {
        // You would show/hide a progress bar here
        binding.progressBar.isVisible = isLoading
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.no_internet_connection)
            .setMessage(R.string.check_internet_connection)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
