package com.example.localgrub.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrub.R
import com.example.localgrub.data.model.FoodItem
import com.example.localgrub.databinding.FragmentHomeBinding
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.ui.adapter.FoodItemAdapter
import com.example.localgrub.ui.components.NoInternetDialogFragment
import com.example.localgrub.ui.sharedviewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), FoodItemAdapter.FoodItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()

    private val foodAdapter: FoodItemAdapter by lazy {
        FoodItemAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchFoodItems()
        viewModel.loadCurrentUser()
    }

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

    private fun setupRecyclerView() {
        binding.foodItemsRecyclerView.adapter = foodAdapter
    }

    private fun setupListeners() {
        binding.viewCartButton.setOnClickListener {
            val cartItems = viewModel.foodItems.value.filter { it.isSelected }
            if (cartItems.isNotEmpty()) {
                sharedHCOViewModel.onSetSelectItemList(cartItems)
                viewModel.loadUser()
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cart -> {
                    showPopupMenu(binding.topAppBar.findViewById(R.id.cart))
                    true
                }

                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { handleUIState(it) }
                }
                launch {
                    viewModel.foodItems.collectLatest { foodAdapter.submitList(it) }
                }
                launch {
                    viewModel.isCartVisible.collectLatest { isVisible ->
                        binding.viewCartButton.isVisible = isVisible
                    }
                }
            }
        }
    }

    private fun handleUIState(state: HomeUIState) {
        when (state) {
            is HomeUIState.Loading -> onSetLoading(true)
            is HomeUIState.DishGetSuccess -> handleDishGetSuccess(state.dishes)
            is HomeUIState.DishGetFailure -> handleDishGetFailure(state.failure)
            is HomeUIState.UserGetFailure -> handleUserGetFailure(state.failure)
            is HomeUIState.OrderState -> navigateToOrder(state)
            is HomeUIState.ProfileState -> navigateToProfile(state)
            HomeUIState.LoginState -> navigateToLogin()
            HomeUIState.NoInternet -> {
                showNoInternetDialog()
                onSetLoading(false)
            }

            HomeUIState.Idle -> {
                updateEmptyStateVisibility(viewModel.foodItems.value.isEmpty())
                onSetLoading(false)
            }
        }
    }

    private fun handleDishGetSuccess(dishes: List<FoodItem>) {
        updateEmptyStateVisibility(dishes.isEmpty())
        if (dishes.isNotEmpty()) {
            val selectedDishes = sharedHCOViewModel.selectItemList.value
            val listWithSelected = dishes.map { item ->
                selectedDishes.find { it.id == item.id } ?: item
            }
            viewModel.onChangeFoodItems(listWithSelected)
        }
        onSetLoading(false)
    }

    private fun handleDishGetFailure(failure: com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure) {
        when (failure) {
            is com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure.DataNotFount -> {
                updateEmptyStateVisibility(true)
            }

            is com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure.InvalidData -> {
                showToast(failure.message)
            }

            com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure.Network -> {
                showNoInternetDialog()
            }

            is com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure.PermissionDenied -> {
                showToast(failure.message)
            }

            is com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure.Unknown -> {
                showToast(failure.cause.message ?: getString(R.string.error))
            }
        }
        onSetLoading(false)
    }

    private fun handleUserGetFailure(failure: GetReqDomainFailure) {
        when (failure) {
            is GetReqDomainFailure.DataNotFound -> {
                val user = viewModel.user.value
                val action =
                    HomeFragmentDirections.actionHomeFragmentToCreateYourProfileFragment(user)
                findNavController().navigate(action)
                viewModel.reset()
            }

            is GetReqDomainFailure.InvalidRequest -> showToast(failure.message)
            GetReqDomainFailure.NoInternet -> showNoInternetDialog()
            is GetReqDomainFailure.PermissionDenied -> showToast(failure.message)
            is GetReqDomainFailure.Unknown -> showToast(
                failure.cause.message ?: getString(R.string.error)
            )

            GetReqDomainFailure.Cancelled -> Unit
        }
        onSetLoading(false)
    }

    private fun updateEmptyStateVisibility(isEmpty: Boolean) {
        binding.showEmptyListStateTextView.isVisible = isEmpty
        binding.orderItemsRecyclerViewContainer.isVisible = !isEmpty
    }

    private fun navigateToOrder(state: HomeUIState.OrderState) {
        sharedHCOViewModel.onSetUser(user = state.user)
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOrderFragment())
        viewModel.reset()
        onSetLoading(false)
    }

    private fun navigateToProfile(state: HomeUIState.ProfileState) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToCreateYourProfileFragment(
                state.user
            )
        )
        viewModel.reset()
        onSetLoading(false)
    }

    private fun navigateToLogin() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAuthenticationFragment())
        viewModel.reset()
        onSetLoading(false)
    }

    private fun showPopupMenu(anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.home_popup_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_history -> {
                        viewModel.user.value?.let { user ->
                            findNavController().navigate(
                                HomeFragmentDirections.actionHomeFragmentToHistoryFragment(
                                    uid = user.uid
                                )
                            )
                        } ?: showToast(getString(R.string.error))
                        true
                    }

                    R.id.action_logout -> {
                        viewModel.logout()
                        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAuthenticationFragment())
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun showNoInternetDialog() {
        NoInternetDialogFragment().show(parentFragmentManager, "NoInternetDialog")
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message ?: getString(R.string.error), Toast.LENGTH_SHORT)
            .show()
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.isVisible = isLoading
        binding.viewCartButton.isEnabled = !isLoading
    }

    override fun onIncreaseQuantity(item: FoodItem) = viewModel.onIncreaseQuantity(item)

    override fun onDecreaseQuantity(item: FoodItem) = viewModel.onDecreaseQuantity(item)

    override fun onSelectItem(item: FoodItem, isSelected: Boolean) =
        viewModel.onSelectItem(item, isSelected)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
