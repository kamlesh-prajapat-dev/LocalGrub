package com.example.localgrub.ui.screens.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.localgrub.R
import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.databinding.FragmentHistoryBinding
import com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrub.ui.adapter.OrderHistoryItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment(), OrderHistoryItemAdapter.OrderHistoryItemClickListener {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private val orderHistoryItemAdapter: OrderHistoryItemAdapter by lazy {
        OrderHistoryItemAdapter(this)
    }
    private val navArgs: HistoryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uid = navArgs.uid
        viewModel.loadOrderHistoryItems(uid)
    }

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
        setupListener()
    }

    private fun setupListener() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
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
        when (state) {
            is HistoryUIState.Failure -> {
                when (val failure = state.failure) {
                    is GetReqDomainFailure.DataNotFount -> {
                        binding.orderItemsRecyclerViewContainer.isVisible = false
                        binding.emptyStateTxt.isVisible = true
                    }

                    is GetReqDomainFailure.InvalidData -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    GetReqDomainFailure.Network -> {
                        showNoInternetDialog()
                    }

                    is GetReqDomainFailure.PermissionDenied -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    is GetReqDomainFailure.Unknown -> {
                        Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
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
                val orders = state.orders
                if (orders.isNotEmpty()) {
                    binding.orderItemsRecyclerViewContainer.isVisible = true
                    binding.emptyStateTxt.isVisible = false
                    viewModel.onSetHistoryOrder(orders)
                } else {
                    binding.orderItemsRecyclerViewContainer.isVisible = false
                    binding.emptyStateTxt.isVisible = true
                }
                onSetLoading(false)
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

    override fun onViewDetailsOrderHistoryItem(item: FetchedOrder) {
        val action = HistoryFragmentDirections.actionHistoryFragmentToEachOrderStatusFragment(item.id)
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        viewModel.reset()
    }
}