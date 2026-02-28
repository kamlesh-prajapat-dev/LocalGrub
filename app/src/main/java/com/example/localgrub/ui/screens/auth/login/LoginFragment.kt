package com.example.localgrub.ui.screens.auth.login

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrub.R
import com.example.localgrub.databinding.FragmentLoginBinding
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.ui.screens.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private val sharedViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.sendOtpButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            viewModel.loginUser(
                phoneNumber = phoneNumber
            )
        }

        binding.phoneNumberEditText.addTextChangedListener {
            binding.phoneNumberEditTextInputLayout.error = null
            binding.phoneNumberEditTextInputLayout.isErrorEnabled = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        LoginUIState.Idle -> {
                            onSetLoading(false)
                        }

                        LoginUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is LoginUIState.Validation -> {
                            binding.phoneNumberEditText.error = it.message
                            binding.phoneNumberEditTextInputLayout.isErrorEnabled = true
                            onSetLoading(false)
                        }

                        LoginUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }

                        is LoginUIState.OtpSent -> {
                            navigateToOtp(it.phoneNumber)
                            onSetLoading(false)
                        }

                        LoginUIState.HomeState -> {
                            val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                            findNavController().navigate(action)
                            onSetLoading(false)
                        }

                        is LoginUIState.UserGetFailure -> {
                            when (val failure = it.failure) {
                                is GetReqDomainFailure.DataNotFound -> Unit
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
                    }
                }
            }
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message ?: getString(R.string.error), Toast.LENGTH_SHORT)
            .show()
    }

    private fun navigateToOtp(phoneNumber: String) {
        val verificationId = sharedViewModel.verificationId.value
        val token = sharedViewModel.token.value
        val otpSentTime = sharedViewModel.otpSentTime.value

        if (verificationId != null && token != null && otpSentTime != 0L) {
            sharedViewModel.setInitialData(
                verificationId = verificationId,
                token = token,
                otpSentTime = otpSentTime
            )
        }
        val action =
            LoginFragmentDirections.actionLoginFragmentToOtpFragment(phoneNumber)
        findNavController().navigate(action)
        viewModel.reset()
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.sendOtpButton.isEnabled = !isLoading
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

        _binding = null
    }
}