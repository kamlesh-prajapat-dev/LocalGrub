package com.example.roti999.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.roti999.data.model.User
import com.example.roti999.ui.adapter.FoodItemAdapter
import com.example.roti999.domain.model.AddOn
import com.example.roti999.domain.model.FoodItem
import com.example.roti999.databinding.FragmentHomeBinding
import com.example.roti999.ui.viewmodel.HomeViewModel
import com.example.roti999.ui.viewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), FoodItemAdapter.FoodItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()

    private lateinit var foodAdapter: FoodItemAdapter

    private var user: User? = null

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
            val cartItems = foodAdapter.currentList.filter { it.isSelected }.map {
                it.copy(addOns = it.addOns.filter { addOn ->
                    addOn.isSelected
                })
            }
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
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodItemAdapter(this)
        binding.foodItemsRecyclerView.adapter = foodAdapter
    }

    private fun observeViewModel() {
        viewModel.foodItems.observe(viewLifecycleOwner) { items ->
            foodAdapter.submitList(items)
        }

        viewModel.isCartVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.viewCartButton.isVisible = isVisible
        }

        viewModel.user.observe(viewLifecycleOwner) {
            user = it
        }
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

    override fun onAddOnSelected(item: FoodItem, addOn: AddOn, isSelected: Boolean) {
        viewModel.onAddOnSelected(item, addOn, isSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
