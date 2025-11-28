package com.example.roti999.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentCreateYourProfileBinding
import com.example.roti999.ui.viewmodel.CreateYourProfileViewModel
import com.example.roti999.ui.viewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateYourProfileFragment : Fragment() {
    private val viewModel: CreateYourProfileViewModel by viewModels()
    private var _binding: FragmentCreateYourProfileBinding? = null
    private val binding get() = _binding!!

    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.saveProfileButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            viewModel.createUser(name, address)
        }
    }

    private fun observeViewModel() {

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.phoneNumberEditText.setText(it.phoneNumber)
            }
        }

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreateYourProfileViewModel.ProfileState.Idle -> {
                    setLoading(false)
                }

                is CreateYourProfileViewModel.ProfileState.Loading -> {
                    setLoading(true)
                }

                is CreateYourProfileViewModel.ProfileState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Profile saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    sharedHCOViewModel.onSetUser(user = viewModel.user.value)
                    setLoading(false)
                    findNavController().navigate(R.id.action_createYourProfileFragment_to_orderFragment) // Assuming you have this action
                }

                is CreateYourProfileViewModel.ProfileState.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

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
