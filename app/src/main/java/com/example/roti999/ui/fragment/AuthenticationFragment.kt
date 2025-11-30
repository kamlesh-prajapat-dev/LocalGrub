package com.example.roti999.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentAuthenticationBinding
import com.example.roti999.domain.model.AuthUiState
import com.example.roti999.ui.viewmodel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthenticationFragment : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthenticationViewModel by viewModels()
    private var verificationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.sendOtpButton.setOnClickListener {
            if (viewModel.isInternetAvailable()) {
                val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
                if (validatePhoneNumber(phoneNumber)) {
                    viewModel.sendOtp(phoneNumber, requireActivity())
                }
            } else {
                showNoInternetDialog()
            }
        }

        binding.verifyOtpButton.setOnClickListener {
            if (viewModel.isInternetAvailable()) {
                val otp = binding.otpEditText.text.toString().trim()
                if (validateOtp(otp)) {
                    verificationId?.let {
                        viewModel.verifyOtp(otp, it)
                    }
                }
            } else {
                showNoInternetDialog()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        setLoading(false)
                        showPhoneNumberInput()
                    }
                    is AuthUiState.Loading -> {
                        setLoading(true)
                    }
                    is AuthUiState.OtpSent -> {
                        setLoading(false)
                        verificationId = state.verificationId
                        showOtpInput()
                        Toast.makeText(requireContext(), "OTP has been sent", Toast.LENGTH_SHORT).show()
                    }
                    is AuthUiState.Success -> {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Authentication successful!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_authenticationFragment_to_homeFragment)
                        viewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Authentication failed: ${state.message}", Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.sendOtpButton.isEnabled = !isLoading
        binding.verifyOtpButton.isEnabled = !isLoading
    }

    private fun showPhoneNumberInput() {
        binding.phoneNumberInputLayout.isVisible = true
        binding.sendOtpButton.isVisible = true
        binding.otpInputLayout.isVisible = false
        binding.verifyOtpButton.isVisible = false
    }

    private fun showOtpInput() {
        binding.phoneNumberInputLayout.isVisible = false
        binding.sendOtpButton.isVisible = false
        binding.otpInputLayout.isVisible = true
        binding.verifyOtpButton.isVisible = true
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        return if (phoneNumber.length == 10) {
            binding.phoneNumberInputLayout.error = null
            true
        } else {
            binding.phoneNumberInputLayout.error = "Please enter a valid 10-digit phone number"
            false
        }
    }

    private fun validateOtp(otp: String): Boolean {
        return if (otp.length == 6) {
            binding.otpInputLayout.error = null
            true
        } else {
            binding.otpInputLayout.error = "Please enter a valid 6-digit OTP"
            false
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding when the view is destroyed
    }
}
