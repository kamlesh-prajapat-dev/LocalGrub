package com.example.localgrub.ui.screens.createprofile

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
import androidx.navigation.fragment.navArgs
import com.example.localgrub.R
import com.example.localgrub.databinding.FragmentCreateYourProfileBinding
import com.example.localgrub.domain.model.failure.WriteReqDomainFailure
import com.example.localgrub.ui.sharedviewmodel.SharedHCOViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateYourProfileFragment : Fragment() {
    private val viewModel: CreateYourProfileViewModel by viewModels()
    private var _binding: FragmentCreateYourProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedHCOViewModel: SharedHCOViewModel by activityViewModels()
    private val navArgs: CreateYourProfileFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = navArgs.user
        if (user != null) {
            viewModel.onSetUser(user)
        }
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
                viewModel.uiState.collect { state ->
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

            is ProfileUIState.Failure -> {
                when (val failure = state.failure) {
                    WriteReqDomainFailure.Aborted -> {
                        Toast.makeText(requireContext(), "Aborted", Toast.LENGTH_LONG).show()
                    }

                    is WriteReqDomainFailure.AlreadyExists -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    is WriteReqDomainFailure.Cancelled -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.DataLoss -> {
                        Toast.makeText(requireContext(), "Data Loss", Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.DeadlineExceeded -> {
                        Toast.makeText(requireContext(), "Deadline Exceed", Toast.LENGTH_LONG)
                            .show()
                    }

                    WriteReqDomainFailure.FailedPrecondition -> {
                        Toast.makeText(requireContext(), "Failed Precondition", Toast.LENGTH_LONG)
                            .show()
                    }

                    WriteReqDomainFailure.Internal -> {
                        Toast.makeText(requireContext(), "Internal", Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.InvalidArgument -> {
                        Toast.makeText(requireContext(), "Invalid Argument", Toast.LENGTH_LONG)
                            .show()
                    }

                    WriteReqDomainFailure.NetworkUnavailable -> {
                        showNoInternetDialog()
                    }

                    is WriteReqDomainFailure.NotFound -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.OutOfRange -> {}
                    is WriteReqDomainFailure.PermissionDenied -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.ResourceExhausted -> {
                        Toast.makeText(requireContext(), "Resource Exhausted", Toast.LENGTH_LONG)
                            .show()

                    }

                    is WriteReqDomainFailure.Unauthenticated -> {
                        Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                    }

                    WriteReqDomainFailure.Unimplemented -> {
                        Toast.makeText(requireContext(), "Unimplemented", Toast.LENGTH_LONG).show()
                    }

                    is WriteReqDomainFailure.Unknown -> {
                        Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                setLoading(false)
            }

            is ProfileUIState.Success -> {
                sharedHCOViewModel.onSetUser(user = state.user)
                Toast.makeText(
                    requireContext(),
                    "Profile saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                navigateAction()
                setLoading(false)
            }

            is ProfileUIState.ValidationErrors -> {
                val msgForName = state.msgForName
                if (msgForName != null) {
                    binding.nameEditText.error = msgForName
                    binding.nameInputLayout.isErrorEnabled = true
                }

                val msgForAddress = state.msgForAddress
                if (msgForAddress != null) {
                    binding.addressEditText.error = msgForAddress
                    binding.addressInputLayout.isErrorEnabled = true
                }

                val noneToSave = state.noneToSave
                if (noneToSave != null) {
                    Toast.makeText(requireContext(), noneToSave, Toast.LENGTH_SHORT).show()
                }
                setLoading(false)
            }

            ProfileUIState.NoInternet -> {
                showNoInternetDialog()
                setLoading(false)
            }
        }
    }

    private fun navigateAction() {
        findNavController().navigateUp()
    }

    private fun setupListeners() {
        binding.addressEditText.addTextChangedListener {
            binding.addressEditText.error = null
            binding.addressInputLayout.isErrorEnabled = false
        }

        binding.nameEditText.addTextChangedListener {
            binding.nameEditText.error = null
            binding.nameInputLayout.isErrorEnabled = false
        }

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
        binding.progressBar.isVisible = isLoading
        binding.saveProfileButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}