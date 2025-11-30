package com.example.roti999.ui.fragment

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.data.model.Order
import com.example.roti999.databinding.FragmentHistoryBinding
import com.example.roti999.databinding.FragmentHomeBinding
import com.example.roti999.databinding.OrderHistoryItemBinding
import com.example.roti999.ui.adapter.FoodItemAdapter
import com.example.roti999.ui.adapter.OrderHistoryItemAdapter
import com.example.roti999.ui.viewmodel.HistoryViewModel
import com.example.roti999.ui.viewmodel.SharedHFToEOSFViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment(), OrderHistoryItemAdapter.OrderHistoryItemClickListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private val sharedHFToEOSFViewModel: SharedHFToEOSFViewModel by activityViewModels()
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

    private fun setupRecyclerView() {
        orderHistoryItemAdapter = OrderHistoryItemAdapter(this)
        binding.orderHistoryRecyclerView.adapter = orderHistoryItemAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyOrders.collect {
                orderHistoryItemAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isNetworkAvailable.collect {
                if (!it) showNoInternetDialog()
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

    override fun onViewDetailsOrderHistoryItem(item: Order) {
        sharedHFToEOSFViewModel.onSetOrder(item)
        val action = HistoryFragmentDirections.actionHistoryFragmentToEachOrderStatusFragment()
        findNavController().navigate(action)
    }
}