package com.example.localgrub.ui.screens.orderstatus

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.localgrub.R
import com.example.localgrub.data.model.FetchedOrder
import com.example.localgrub.databinding.FragmentOrderStatusBinding
import com.example.localgrub.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrub.domain.mapper.firebase.WriteReqDomainFailure
import com.example.localgrub.ui.adapter.OrderSummaryAdapter
import com.example.localgrub.util.OrderStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderStatusFragment : Fragment() {
    private var _binding: FragmentOrderStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderStatusViewModel by viewModels()
    private val orderSummaryAdapter: OrderSummaryAdapter by lazy {
        OrderSummaryAdapter()
    }
    val navArgs: OrderStatusFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = navArgs.orderId
        viewModel.observeOrderById(orderId)
    }

    private data class Step(
        val icon: ImageView,
        val title: TextView,
        val line: View?,
        val status: String
    )

    private lateinit var steps: List<Step>

    private enum class StepState { COMPLETE, INCOMPLETE, CANCELLED }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModels()
    }

    private fun setupUI() {
        binding.orderedItemsRecyclerView.adapter = orderSummaryAdapter

        steps = listOf(
            Step(binding.step1Icon, binding.step1Title, binding.step1Line, OrderStatus.PLACED),
            Step(binding.step2Icon, binding.step2Title, binding.step2Line, OrderStatus.CONFIRMED),
            Step(binding.step3Icon, binding.step3Title, binding.step3Line, OrderStatus.PREPARING),
            Step(
                binding.step4Icon,
                binding.step4Title,
                binding.step4Line,
                OrderStatus.OUT_FOR_DELIVERY
            ),
            Step(binding.step5Icon, binding.step5Title, null, OrderStatus.DELIVERED)
        )

        binding.topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelOrder()
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    setLoading(uiState is OrderStatusUIState.Loading)
                    when (uiState) {
                        is OrderStatusUIState.Success -> {
                            Toast.makeText(
                                requireContext(),
                                "Order Successfully Cancelled.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is OrderStatusUIState.OrderGetFailure -> {
                            when (val failure = uiState.failure) {
                                is GetReqDomainFailure.DataNotFount -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is GetReqDomainFailure.InvalidData -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                GetReqDomainFailure.Network -> {
                                    showNoInternetDialog()
                                }

                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.cause.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        is OrderStatusUIState.OrderGetSuccess -> {
                            viewModel.onSetOrder(uiState.order)
                        }

                        is OrderStatusUIState.NoInternet -> {
                            showNoInternetDialog()
                        }

                        is OrderStatusUIState.CancelOrderFailure -> {
                            when (val failure = uiState.failure) {
                                is WriteReqDomainFailure.Cancelled -> Unit
                                is WriteReqDomainFailure.DataNotFound -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                WriteReqDomainFailure.NoInternet -> {
                                    showNoInternetDialog()
                                }

                                is WriteReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
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

                                is WriteReqDomainFailure.ValidationError -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.order.collect { order ->
                    if (order != null) {
                        updateUiWithOrder(order)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect {
                    if (it != null)
                        binding.addressTextView.text = it.address
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUiWithOrder(order: FetchedOrder) {
        orderSummaryAdapter.submitList(order.items)
        viewModel.onSetSelectItemList(order.items)

        binding.itemTotalTextView.text = "Rs. ${order.totalPrice}"
        val deliveryFee = 0.0
        binding.deliveryFeeTextView.text = "Rs. $deliveryFee"
        binding.grandTotalTextView.text = "Rs. ${order.totalPrice + deliveryFee}"

        updateStatusStepper(order)
    }

    private fun updateStatusStepper(order: FetchedOrder) {
        val currentStatus = order.status
        val isCancelable = currentStatus in listOf(
            OrderStatus.PLACED,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING
        )
        binding.cancelButton.isVisible = isCancelable

        if (currentStatus == OrderStatus.CANCELLED) {
            val lastCompletedIndex =
                steps.indexOfFirst { it.status == order.previousStatus }.takeIf { it != -1 } ?: -1

            steps.forEachIndexed { index, step ->
                when {
                    index <= lastCompletedIndex -> {
                        setStepVisibility(step, true)
                        applyStateToStep(step, StepState.COMPLETE)
                    }

                    index == lastCompletedIndex + 1 -> {
                        setStepVisibility(step, true)
                        applyStateToStep(step, StepState.CANCELLED)
                        step.line?.isVisible =
                            false // Hide the line pointing away from the CANCELLED status
                    }

                    else -> {
                        setStepVisibility(step, false) // Hide all subsequent steps
                    }
                }
            }
        } else {
            // Normal flow for non-cancelled orders
            steps.forEach { setStepVisibility(it, true) }
            val statusIndex = steps.indexOfFirst { it.status == currentStatus }
            steps.forEachIndexed { index, step ->
                val state = if (index <= statusIndex) StepState.COMPLETE else StepState.INCOMPLETE
                applyStateToStep(step, state)
            }
        }
    }

    private fun setStepVisibility(step: Step, isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        step.icon.visibility = visibility
        step.title.visibility = visibility
        // Only try to set line visibility if it exists
        step.line?.visibility = visibility
    }

    private fun applyStateToStep(step: Step, state: StepState) {
        val green = ContextCompat.getColor(requireContext(), R.color.green)
        val red = ContextCompat.getColor(requireContext(), R.color.red)
        val orange = ContextCompat.getColor(requireContext(), R.color.orange)

        val iconRes: Int
        val textColor: Int
        val lineBgColor: Int
        val typeFace: Int

        when (state) {
            StepState.COMPLETE -> {
                iconRes = R.drawable.stepper_background_complete
                textColor = green
                lineBgColor = green
                typeFace = Typeface.BOLD
            }

            StepState.INCOMPLETE -> {
                iconRes = R.drawable.stepper_background_incomplete
                textColor = orange
                lineBgColor = orange
                typeFace = Typeface.NORMAL
            }

            StepState.CANCELLED -> {
                iconRes = R.drawable.stepper_background_cancel
                textColor = red
                lineBgColor = red
                typeFace = Typeface.BOLD
            }
        }

        step.icon.setImageResource(iconRes)
        step.title.text =
            if (state == StepState.CANCELLED) {
                OrderStatus.CANCELLED
            } else step.status
        step.title.setTextColor(textColor)
        step.title.setTypeface(null, typeFace)
        step.line?.setBackgroundColor(lineBgColor)
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
        binding.cancelButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
