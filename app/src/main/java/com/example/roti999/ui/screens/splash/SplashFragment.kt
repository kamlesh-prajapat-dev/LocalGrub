package com.example.roti999.ui.screens.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.roti999.R
import com.example.roti999.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

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
            // Delay to show the splash screen for a short period
            delay(3000)

            viewModel.uiState.collect {
                when (it) {
                    SplashNavigationState.Home -> {
                        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    }

                    SplashNavigationState.Authentication -> {
                        findNavController().navigate(R.id.action_splashFragment_to_authenticationFragment)
                    }

                    SplashNavigationState.Idle -> { /* Initial State: Here Show UI */}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by nulling out the binding
        _binding = null
    }
}