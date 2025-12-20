package com.example.roti999.ui.screens.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentAuthenticationBinding
import com.example.roti999.ui.components.NoInternetDialogFragment
import com.example.roti999.ui.screens.auth.AuthUIEvent.ShowNoInternetDialog
import com.example.roti999.ui.screens.auth.AuthUIEvent.ShowToast
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Exception

@AndroidEntryPoint
class AuthenticationFragment : Fragment() {
    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthenticationViewModel by viewModels()
    private var noInternetDialog: DialogFragment? = null

    private var verificationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        // Validation cleanup: clear errors when user starts typing
        binding.phoneNumberEditText.doAfterTextChanged {
            binding.phoneNumberEditTextInputLayout.error = null
            binding.phoneNumberEditTextInputLayout.isErrorEnabled = false
        }

        binding.otpEditText.doAfterTextChanged {
            binding.otpEditTextInputLayout.error = null
            binding.otpEditTextInputLayout.isErrorEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.sendOtpButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            viewModel.sendOtp(phoneNumber, requireActivity())
        }

        binding.verifyOtpButton.setOnClickListener {
            val otp = binding.otpEditText.text.toString().trim()
            viewModel.verifyOtp(otp)
        }

        binding.resendOtpBtn.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            viewModel.resendOtp(phoneNumber, requireActivity())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect {
                    handleAuthUIState(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleAuthUIEvent(event)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.verificationId.collect {
                    if (it != null) {
                        verificationId = it
                    }
                }
            }
        }
    }

    private fun handleAuthUIEvent(event: AuthUIEvent) {
        when (event) {
            is ShowToast -> Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                .show()

            ShowNoInternetDialog -> showNoInternetDialog()
        }
    }

    private fun handleAuthUIState(state: AuthUiState) {
        when (state) {
            is AuthUiState.Idle -> {
                onSetLoading(false)
                showPhoneNumberInput()
            }

            is AuthUiState.Loading -> {
                onSetLoading(true)
            }

            is AuthUiState.OtpSent -> {
                onSetLoading(false)
            }

            is AuthUiState.Success -> {
                viewModel.onSetUIEvent(ShowToast("Authentication successful!"))
                onSetLoading(false)
                findNavController().navigate(R.id.action_authenticationFragment_to_homeFragment)
                viewModel.resetState()
            }

            is AuthUiState.AuthFailure -> {
                handleException(state.e)
                onSetLoading(false)
            }

            AuthUiState.NoInternet -> {
                viewModel.onSetUIEvent(ShowNoInternetDialog)
                onSetLoading(false)
            }

            is AuthUiState.ValidationError -> {
                if (state.field) {
                    binding.phoneNumberEditTextInputLayout.error = state.message
                    binding.phoneNumberEditTextInputLayout.isErrorEnabled = true
                } else {
                    binding.otpEditTextInputLayout.error = state.message
                    binding.otpEditTextInputLayout.isErrorEnabled = true
                }
                onSetLoading(false)
            }

            AuthUiState.OtpLayout -> {
                showOtpInput()
                viewModel.onSetUIEvent(ShowToast("OTP has been sent"))
                onSetLoading(false)
            }
        }
    }

    private fun handleException(e: Exception?) {
        when (e) {
            is FirebaseNetworkException -> {
                viewModel.onSetUIEvent(ShowNoInternetDialog)
            }

            is FirebaseAuthInvalidCredentialsException -> {
                binding.otpEditTextInputLayout.error = "Invalid OTP"
                binding.otpEditTextInputLayout.isErrorEnabled = true
            }

            else -> {
                viewModel.onSetUIEvent(ShowToast("Authentication failed: ${e?.message}"))
                viewModel.resetState()
            }
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.sendOtpButton.isEnabled = !isLoading
        binding.verifyOtpButton.isEnabled = !isLoading && verificationId != null
        binding.resendOtpBtn.isEnabled = !isLoading && verificationId != null
    }

    private fun showPhoneNumberInput() {
        binding.phoneNumberInputLayout.isVisible = true
        binding.otpInputLayout.isVisible = false
        binding.appBarLayout.isVisible = false
    }

    private fun showOtpInput() {
        binding.phoneNumberInputLayout.isVisible = false
        binding.otpInputLayout.isVisible = true
        binding.appBarLayout.isVisible = true

        // Set up navigation icon on Click Listener
        binding.topAppBar.setNavigationOnClickListener {
            viewModel.resetState()
        }
    }

    private fun showNoInternetDialog() {
        if (noInternetDialog?.isAdded == true) return
        noInternetDialog = NoInternetDialogFragment()
        noInternetDialog?.show(childFragmentManager, "NoInternetDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding when the view is destroyed
    }
}
