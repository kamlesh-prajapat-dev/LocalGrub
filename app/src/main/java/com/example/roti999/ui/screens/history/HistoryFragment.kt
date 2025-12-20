package com.example.roti999.ui.screens.history

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.data.model.Order
import com.example.roti999.databinding.FragmentHistoryBinding
import com.example.roti999.ui.adapter.OrderHistoryItemAdapter
import com.example.roti999.ui.sharedviewmodel.SharedHFToCPFViewModel
import com.example.roti999.ui.sharedviewmodel.SharedHFToEOSFViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment(), OrderHistoryItemAdapter.OrderHistoryItemClickListener {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private val sharedHFToEOSFViewModel: SharedHFToEOSFViewModel by activityViewModels()
    private val sharedHFToCPFViewModel: SharedHFToCPFViewModel by activityViewModels()
    private lateinit var orderHistoryItemAdapter: OrderHistoryItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrderHistoryItems()
    }

    private fun setupRecyclerView() {
        orderHistoryItemAdapter = OrderHistoryItemAdapter(this)
        binding.orderHistoryRecyclerView.adapter = orderHistoryItemAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyOrders.collect {
                    orderHistoryItemAdapter.submitList(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    handleUIState(it)
                }
            }
        }
    }

    private fun handleUIState(state: HistoryUIState) {
        when(state) {
            is HistoryUIState.Error -> {
                Toast.makeText(requireContext(), state.e.message, Toast.LENGTH_SHORT).show()
                onSetLoading(false)
            }
            HistoryUIState.Idle -> {
                onSetLoading(false)
            }
            HistoryUIState.Loading -> {
                onSetLoading(true)
            }
            HistoryUIState.NoInternet -> {
                showNoInternetDialog()
                onSetLoading(false)
            }
            is HistoryUIState.Success -> {
                viewModel.onSetHistoryOrder(state.orders)
                onSetLoading(false)
            }

            HistoryUIState.NavigateToCreateProfile -> {
                sharedHFToCPFViewModel.onSetIsNavigate(true)
                val action = HistoryFragmentDirections.actionHistoryFragmentToCreateYourProfileFragment()
                findNavController().navigate(action)
                viewModel.reset()
            }
        }
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

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }


    override fun onViewDetailsOrderHistoryItem(item: Order) {
        sharedHFToEOSFViewModel.onSetOrder(item)
        val action = HistoryFragmentDirections.actionHistoryFragmentToEachOrderStatusFragment()
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        viewModel.reset()
    }
}