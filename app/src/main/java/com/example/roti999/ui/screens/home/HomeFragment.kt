package com.example.roti999.ui.screens.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.data.model.User
import com.example.roti999.ui.adapter.FoodItemAdapter
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.databinding.FragmentHomeBinding
import com.example.roti999.ui.components.NoInternetDialogFragment
import com.example.roti999.ui.sharedviewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), FoodItemAdapter.FoodItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private lateinit var foodAdapter: FoodItemAdapter
    private var user: User? = null
    private var noInternetDialog: DialogFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.viewCartButton.setOnClickListener {
            // Handle the click event for the "View Cart" button here
            val cartItems = foodAdapter.currentList.filter { it.isSelected }

            if (cartItems.isNotEmpty()) {
                sharedHCOViewModel.onSetSelectItemList(cartItems)
            }
            if (user != null) {
                sharedHCOViewModel.onSetUser(user)
                val action = HomeFragmentDirections.actionHomeFragmentToOrderFragment()
                findNavController().navigate(action)
            } else {
                val action = HomeFragmentDirections.actionHomeFragmentToCreateYourProfileFragment()
                findNavController().navigate(action)
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cart -> {
                    // Show the popup menu
                    showPopupMenu(binding.topAppBar.findViewById(R.id.cart))
                    true
                }

                else -> false
            }
        }
    }

    private fun showPopupMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.home_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_history -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToHistoryFragment()
                    findNavController().navigate(action)
                    true
                }

                R.id.action_logout -> {
                    viewModel.logout()
                    val action = HomeFragmentDirections.actionHomeFragmentToAuthenticationFragment()
                    findNavController().navigate(action)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodItemAdapter(this)
        binding.foodItemsRecyclerView.adapter = foodAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    handleUIState(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foodItems.collect {
                    foodAdapter.submitList(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCartVisible.collect { isVisible ->
                    binding.viewCartButton.isVisible = isVisible
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect {
                    user = it
                }
            }
        }
    }

    private fun handleUIState(state: HomeUIState) {
        when (state) {
            is HomeUIState.Success -> {
                val list = state.dishes
                if (list.isNotEmpty()) {
                    binding.showEmptyListStateTextView.isVisible = false
                    binding.foodItemsRecyclerView.isVisible = true
                    viewModel.onChangeFoodItems(state.dishes)
                } else {
                    binding.showEmptyListStateTextView.isVisible = true
                    binding.showEmptyListStateTextView.text = "No dishes found"
                    binding.foodItemsRecyclerView.isVisible = false
                }
                onSetLoading(false)
                viewModel.loadUser()
            }

            is HomeUIState.Error -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(state.e.message)
                    .show()
                onSetLoading(false)
            }

            is HomeUIState.Loading -> {
                onSetLoading(true)
            }

            HomeUIState.Idle -> {
                onSetLoading(false)
            }

            HomeUIState.NoInternet -> {
                onSetLoading(false)
                showNoInternetDialog()
            }

            is HomeUIState.SuccessUser -> {
                val user = state.user
                viewModel.setUser(user)
               onSetLoading(false)
            }

            HomeUIState.NavigateToLogin -> {
                onSetLoading(false)
                val action = HomeFragmentDirections.actionHomeFragmentToAuthenticationFragment()
                findNavController().navigate(action)
            }
        }
    }
    private fun showNoInternetDialog() {
        if (noInternetDialog?.isAdded == true) return
        noInternetDialog = NoInternetDialogFragment()
        noInternetDialog?.show(childFragmentManager, "NoInternetDialog")
    }
    private fun onSetLoading(flag: Boolean) {
        binding.loadingIndicator.isVisible = flag
    }

    override fun onIncreaseQuantity(item: FoodItem) {
        viewModel.onIncreaseQuantity(item)
    }

    override fun onDecreaseQuantity(item: FoodItem) {
        viewModel.onDecreaseQuantity(item)
    }

    override fun onSelectItem(item: FoodItem, isSelected: Boolean) {
        viewModel.onSelectItem(item, isSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
