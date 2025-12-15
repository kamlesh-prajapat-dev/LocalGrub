package com.example.roti999.ui.screens.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    private var navigationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        SplashNavigationState.Home -> navigateWithDelay {
                            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                        }

                        SplashNavigationState.Authentication -> navigateWithDelay {
                            findNavController().navigate(R.id.action_splashFragment_to_authenticationFragment)
                        }

                        SplashNavigationState.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun navigateWithDelay(action: () -> Unit) {
        if (navigationJob?.isActive == true) return

        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            action()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}