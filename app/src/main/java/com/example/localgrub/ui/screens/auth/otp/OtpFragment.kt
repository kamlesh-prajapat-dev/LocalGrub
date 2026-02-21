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
import com.example.localgrub.domain.model.failure.AuthError
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure
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
            val verificationId = sharedViewModel.verificationId.value
            val token = sharedViewModel.token.value
            val otpSentTime = sharedViewModel.otpSentTime.value
            val oldPhoneNumber = sharedViewModel.phoneNumber.value

            if (verificationId != null && token != null && otpSentTime != 0L) {
                viewModel.setInitialData(
                    verificationId = verificationId,
                    token = token,
                    otpSentTime = otpSentTime
                )
            }

            viewModel.sentOtp(
                activity = requireActivity(),
                phoneNumber = newPhoneNumber,
                oldPhoneNumber = oldPhoneNumber
            )
        } else {
            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT)
                .show()
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
        val verificationId = viewModel.verificationId.value
        val token = viewModel.token.value
        val otpSentTime = viewModel.otpSentTime.value

        if (verificationId != null && token != null && otpSentTime != 0L) {
            sharedViewModel.setInitialData(
                verificationId = verificationId,
                token = token,
                otpSentTime = otpSentTime
            )
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
            val token = viewModel.token.value
            if (phoneNumber != null && token != null) {
                viewModel.resendOtp(
                    phoneNumber = phoneNumber,
                    token = token,
                    activity = requireActivity()
                )
            } else
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is OtpUIState.AuthFailure -> {
                            when (it.error) {
                                is AuthError.InvalidOtp -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Invalid credentials",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is AuthError.FirebaseError -> {
                                    Toast.makeText(
                                        requireContext(),
                                        it.error.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is AuthError.NetworkError -> {
                                    showNoInternetDialog()
                                }

                                is AuthError.Unknown -> {
                                    Toast.makeText(
                                        requireContext(),
                                        it.error.throwable?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        OtpUIState.Idle -> {
                            onSetLoading(false)
                        }

                        OtpUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is OtpUIState.Success -> {
                            val user = it.user
                            if (user != null) {
                                viewModel.saveUserToken(user.uid)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong. Please try after some time.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onSetLoading(false)
                        }

                        is OtpUIState.Validation -> {
                            binding.otpEditText.error = it.message
                            onSetLoading(false)
                        }

                        is OtpUIState.OnVerificationCompleted -> {
                            val credential = it.credential
                            val smsCode = credential.smsCode
                            if (smsCode != null)
                                binding.otpEditText.setText(smsCode)
                            else
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            viewModel.verifyOtp(credential)
                            onSetLoading(false)
                        }

                        is OtpUIState.Verification -> {
                            viewModel.setInitialData(
                                verificationId = it.verificationId,
                                token = it.token,
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

                        is OtpUIState.TokenUpdateFailure -> {
                            when (val failure = it.failure) {
                                WriteReqDomainFailure.Aborted -> {
                                    Toast.makeText(requireContext(), "Aborted", Toast.LENGTH_LONG)
                                        .show()
                                }

                                is WriteReqDomainFailure.AlreadyExists -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is WriteReqDomainFailure.Cancelled -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.DataLoss -> {
                                    Toast.makeText(requireContext(), "Data Loss", Toast.LENGTH_LONG)
                                        .show()
                                }

                                WriteReqDomainFailure.DeadlineExceeded -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Deadline Exceed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.FailedPrecondition -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed Precondition",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.Internal -> {
                                    Toast.makeText(requireContext(), "Internal", Toast.LENGTH_LONG)
                                        .show()
                                }

                                WriteReqDomainFailure.InvalidArgument -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Invalid Argument",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.NetworkUnavailable -> {
                                    showNoInternetDialog()
                                }

                                is WriteReqDomainFailure.NotFound -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.OutOfRange -> {}
                                is WriteReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.ResourceExhausted -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Resource Exhausted",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }

                                is WriteReqDomainFailure.Unauthenticated -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.Unimplemented -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Unimplemented",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is WriteReqDomainFailure.Unknown -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.cause.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            navigateToHome()
                            onSetLoading(false)
                        }

                        is OtpUIState.TokenUpdateSuccess -> {
                            navigateToHome()
                            onSetLoading(false)
                        }

                        OtpUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
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