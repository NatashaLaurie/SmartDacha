package com.example.dacha.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dacha.R
import com.example.dacha.databinding.FragmentLoginBinding
import com.example.dacha.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            requireActivity(),
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {

                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "Нет интернета" // Optional
                noInternetConnectionMessage =
                    "Проверьте пж интернет подключение и попробуйте снова." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Плиз верните интернет" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Моб. данные" // Optional

                onAirplaneModeTitle = "Нет интернета" // Optional
                onAirplaneModeMessage = "Вы включили режим полёта." // Optional
                pleaseTurnOffText = "Плиз отключите" // Optional
                airplaneModeOffButtonText = "Режим полёта" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()

        observe()
        binding.btnLogin.setOnClickListener {
            if (validate()) {
                viewModel.login(
                    email = binding.etEmail.text.toString().trim(),
                    password = binding.etPassword.text.toString().trim()
                )
            }
        }

    }

    private fun observe() {
        viewModel.login.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnLogin.text = ""
                    binding.loginProgress.visibility = View.VISIBLE
                }
                is UiState.Failure -> {
                    binding.btnLogin.text = "Войти"
                    binding.loginProgress.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()

                }
                is UiState.Success -> {
                    binding.btnLogin.text = "Войти"
                    binding.loginProgress.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        if (binding.etEmail.text.isNullOrEmpty()) {
            isValid = false
            if (binding.etPassword.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Введите логин и пароль", Toast.LENGTH_LONG).show()
            } else Toast.makeText(requireContext(), R.string.invalid_login, Toast.LENGTH_LONG)
                .show()
        } else if (binding.etPassword.text.isNullOrEmpty()) {
            isValid = false
            Toast.makeText(requireContext(), R.string.invalid_pass, Toast.LENGTH_LONG).show()
        }
        return isValid
    }


    override fun onStart() {
        super.onStart()
        viewModel.getSession { user ->
            if (user != null) {
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}