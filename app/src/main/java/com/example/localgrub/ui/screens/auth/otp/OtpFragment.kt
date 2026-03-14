package com.example.localgrub.ui.screens.auth.otp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.localgrub.R
import com.example.localgrub.databinding.FragmentOtpBinding
import com.example.localgrub.domain.model.failure.GetReqDomainFailure
import com.example.localgrub.ui.screens.auth.AuthViewModel
import com.example.localgrub.util.AppConstant.OTP_VALIDITY_MS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpFragment : Fragment() {
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OtpViewModel by viewModels()
    private val sharedViewModel: AuthViewModel by activityViewModels()
    private var timer: CountDownTimer? = null
    private val navArgs: OtpFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newPhoneNumber = navArgs.phoneNumber
        if (newPhoneNumber.isNotBlank()) {
            val response = sharedViewModel.response.value
            val otpSentTime = sharedViewModel.otpSentTime.value

            if (response != null && otpSentTime != 0L) {
                viewModel.setInitialData(
                    response = response,
                    otpSentTime = otpSentTime,
                    phoneNumber = newPhoneNumber
                )
            }
        } else {
            showToast("Something went wrong.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListener()
        observeViewModel()
        startTimer()
    }

    private fun setupUI() {
        binding.tvSubtitle.text = getString(R.string.otp_subtitle, viewModel.phoneNumber.value)
    }

    private fun startTimer() {
        // Cancel any existing timer
        timer?.cancel()

        binding.tvResendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.VISIBLE

        val otpSentTime = viewModel.otpSentTime.value
        val oldPhoneNumber = sharedViewModel.phoneNumber.value
        val newPhoneNumber = viewModel.phoneNumber.value
        val remainingMs = if ((otpSentTime == 0L || System.currentTimeMillis() - otpSentTime >= OTP_VALIDITY_MS) && newPhoneNumber != oldPhoneNumber) {
            OTP_VALIDITY_MS
        } else {
            OTP_VALIDITY_MS - (System.currentTimeMillis() - otpSentTime)
        }
        val safeRemainingMs = maxOf(remainingMs, 0L)

        timer = object : CountDownTimer(safeRemainingMs, 1000) {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val correctedOtpSentTime =
                    System.currentTimeMillis() - (OTP_VALIDITY_MS - millisUntilFinished)
                viewModel.onSetOtpSentTime(correctedOtpSentTime)
                val seconds = millisUntilFinished / 1000
                _binding?.let {
                    it.tvTimer.text = "Resend in 00:${String.format("%02d", seconds)}"
                }
            }

            override fun onFinish() {
                _binding?.let {
                    it.tvResendOtp.visibility = View.VISIBLE
                    it.tvTimer.visibility = View.GONE
                }
            }
        }.start()
    }

    private fun navigationTask() {
        val response = viewModel.response.value
        val otpSentTime = viewModel.otpSentTime.value
        val phoneNumber = viewModel.phoneNumber.value

        if (response != null && otpSentTime != 0L && phoneNumber != null) {
            sharedViewModel.setInitialData(
                response = response,
                otpSentTime = otpSentTime,
                phoneNumber = phoneNumber
            )
        } else {
            showToast("Something went wrong.")
        }
        findNavController().navigateUp()
    }

    private fun setupListener() {
        binding.topAppBar.setNavigationOnClickListener {
            navigationTask()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigationTask()
                }
            }
        )

        binding.otpEditText.addTextChangedListener {
            binding.otpEditText.error = null
        }

        binding.verifyOtpButton.setOnClickListener {
            val otp = binding.otpEditText.text.toString().trim()
            viewModel.verifyOtp(otp)
        }

        binding.tvResendOtp.setOnClickListener {
            val phoneNumber = viewModel.phoneNumber.value
            val response = viewModel.response.value
            if (phoneNumber != null && response != null) {
                viewModel.resendOtp(
                    response = response,
                    phoneNumber = phoneNumber,
                )
            } else
                showToast("Something went wrong.")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {

                        OtpUIState.Idle -> {
                            onSetLoading(false)
                        }

                        OtpUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is OtpUIState.Validation -> {
                            binding.otpEditText.error = it.message
                            onSetLoading(false)
                        }

                        OtpUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }

                        is OtpUIState.OtpSuccessfullySent -> {
                            viewModel.setInitialData(
                                response = it.response,
                                otpSentTime = it.otpSentTime
                            )
                            Toast.makeText(
                                requireContext(),
                                it.message,
                                Toast.LENGTH_LONG
                            ).show()
                            val isTimerStart = viewModel.isTimerStart.value
                            if(isTimerStart) startTimer()
                            onSetLoading(false)
                        }

                        is OtpUIState.LoginSuccess -> {
                            val user = it.user
                            viewModel.saveUserToken(user.uid)
                            onSetLoading(false)
                        }

                        is OtpUIState.Failure -> {
                            when (val failure = it.failure) {
                                is GetReqDomainFailure.DataNotFound -> showToast(failure.message)
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

                        OtpUIState.HomeState -> {
                            navigateToHome()
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

    private fun navigateToHome() {
        val action =
            OtpFragmentDirections.actionOtpFragmentToHomeFragment()
        findNavController().navigate(action)
        viewModel.reset()
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

        binding.tvResendOtp.isEnabled = !isLoading
        binding.verifyOtpButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        timer = null
        _binding = null
    }
}