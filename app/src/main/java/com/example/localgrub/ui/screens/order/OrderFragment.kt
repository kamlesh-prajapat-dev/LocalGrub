package com.example.localgrub.ui.screens.order

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
import com.example.localgrub.R
import com.example.localgrub.data.model.firebase.SelectedDish
import com.example.localgrub.ui.adapter.OrderSummaryAdapter
import com.example.localgrub.databinding.FragmentOrderBinding
import com.example.localgrub.domain.mapper.firebase.WriteReqDomainFailure
import com.example.localgrub.ui.sharedviewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderFragment : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private val viewModel: OrderViewModel by viewModels()
    private val orderSummaryAdapter: OrderSummaryAdapter by lazy {
        OrderSummaryAdapter()
    }

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

        observeSharedViewModel()
        onSetRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalPrice.collect {
                    binding.totalTextView.text = "Rs. $it"
                    binding.deliveryFeeTextView.text = "Rs. 0.0"
                    binding.grandTotalPriceTextView.text = "Rs. $it"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderUIState.collect { state ->
                    handleUIState(state)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedDishes.collect {
                    if (it.isNotEmpty()) {
                        orderSummaryAdapter.submitList(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    if (user != null) {
                        binding.nameTextView.text = user.name
                        binding.phoneNumberTextView.text = user.phoneNumber
                        binding.addressTextView.text = user.address
                    }
                }
            }
        }
    }

    private fun handleUIState(state: OrderUIState) {
        when (state) {
            is OrderUIState.Success -> {
                val order = state.order
                sharedHCOViewModel.clearSelectItemList()
                val action =
                    OrderFragmentDirections.actionOrderFragmentToEachOrderStatusFragment(orderId = order.id)
                findNavController().navigate(action)
                Toast.makeText(requireContext(), "Order successfully placed.", Toast.LENGTH_SHORT)
                    .show()
                onSetLoading(false)
            }

            is OrderUIState.Failure -> {
                when (val failure = state.failure) {
                    is WriteReqDomainFailure.Cancelled -> Unit
                    is WriteReqDomainFailure.DataNotFound -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.NoInternet -> {
                        showNoInternetDialog()
                    }

                    is WriteReqDomainFailure.PermissionDenied -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    is WriteReqDomainFailure.Unknown -> {
                        Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG)
                            .show()
                    }

                    is WriteReqDomainFailure.ValidationError -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }
                }
                onSetLoading(false)
            }

            is OrderUIState.Loading -> {
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
                        viewModel.updateUserData(user)
                    }
                }
            }
        }

        val items = sharedHCOViewModel.selectItemList.value
        if (items.isNotEmpty()) {
            val itemList = items.map {
                SelectedDish(
                    id = it.id,
                    price = it.price,
                    quantity = it.quantity,
                    name = it.name
                )
            }
            viewModel.updateOrderDetails(itemList)
        }
    }

    private fun setupClickListeners() {
        binding.placeOrderButton.setOnClickListener {
            viewModel.placeOrder()
        }
        binding.editProfileButton.setOnClickListener {
            val user = viewModel.user.value
            if (user != null) {
                val action =
                    OrderFragmentDirections.actionOrderFragmentToProfileBuilderFragment(user)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onSetRecyclerView() {
        binding.orderItemsRecyclerView.adapter = orderSummaryAdapter
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.editProfileButton.isEnabled = !isLoading
        binding.placeOrderButton.isEnabled = !isLoading
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
