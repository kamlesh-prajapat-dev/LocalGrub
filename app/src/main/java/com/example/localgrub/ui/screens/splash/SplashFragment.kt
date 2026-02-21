package com.example.localgrub.ui.screens.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrub.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isUserLoggedIn()
    }

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
                        SplashUIState.Home -> navigateAction {
                            val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                            findNavController().navigate(action)
                        }

                        SplashUIState.Authentication -> navigateAction {
                            val action = SplashFragmentDirections.actionSplashFragmentToAuthenticationFragment()
                            findNavController().navigate(action)
                        }

                        SplashUIState.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun navigateAction(action: () -> Unit) {
        action()
        viewModel.reset()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}