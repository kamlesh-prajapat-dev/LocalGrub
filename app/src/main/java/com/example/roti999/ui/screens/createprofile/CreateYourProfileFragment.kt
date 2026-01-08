package com.example.roti999.ui.screens.createprofile

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
import com.example.roti999.databinding.FragmentCreateYourProfileBinding
import com.example.roti999.ui.sharedviewmodel.SharedHCOViewModel
import com.example.roti999.ui.sharedviewmodel.SharedHFToCPFViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateYourProfileFragment : Fragment() {
    private val viewModel: CreateYourProfileViewModel by viewModels()
    private var _binding: FragmentCreateYourProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private val sharedHFToCPFViewModel: SharedHFToCPFViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSharedViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
    }

    private fun observeSharedViewModel() {
        val user = sharedHCOViewModel.user.value
        if (user != null) {
            viewModel.onSetUser(user)
        } else {
            viewModel.loadUser()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect {
                    if (it != null) {
                        binding.phoneNumberEditText.setText(it.phoneNumber)
                        binding.nameEditText.setText(it.name)
                        binding.addressEditText.setText(it.address)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    handleUIState(state)
                }
            }
        }
    }

    private fun handleUIState(state: ProfileUIState) {
        when (state) {
            is ProfileUIState.Idle -> {
                setLoading(false)
            }

            is ProfileUIState.Loading -> {
                setLoading(true)
            }

            is ProfileUIState.Success -> {
                setLoading(false)
            }

            is ProfileUIState.Failure -> {
                Toast.makeText(requireContext(), state.e.message, Toast.LENGTH_SHORT).show()
                setLoading(false)
            }

            is ProfileUIState.UserSavedSuccess -> {
                viewModel.onSetUser(user = state.user)
                sharedHCOViewModel.onSetUser(user = viewModel.user.value)
                Toast.makeText(
                    requireContext(),
                    "Profile saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                navigateAction()
                setLoading(false)
            }

            is ProfileUIState.ValidationErrors -> {
                Toast.makeText(requireContext(), state.errors, Toast.LENGTH_SHORT).show()
                setLoading(false)
            }

            ProfileUIState.NavigateToLogin -> {
                val action =
                    CreateYourProfileFragmentDirections.actionCreateYourProfileFragmentToAuthenticationFragment()
                findNavController().navigate(action)
                setLoading(false)
            }

            ProfileUIState.NoInternet -> {
                showNoInternetDialog()
                setLoading(false)
            }
        }
    }

    private fun navigateAction() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedHFToCPFViewModel.isNavigate.collect {
                    if (it) {
                        findNavController().navigateUp()
                    } else {
                        findNavController().navigate(R.id.action_createYourProfileFragment_to_orderFragment)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.saveProfileButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            viewModel.editUser(name, address)
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

    private fun setLoading(isLoading: Boolean) {
        // You would show/hide a progress bar here
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        sharedHFToCPFViewModel.reset()
    }
}