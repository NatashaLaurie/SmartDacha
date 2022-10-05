package com.example.dacha.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dacha.NetworkViewModel
import com.example.dacha.R
import com.example.dacha.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.managers.FragmentComponentManager

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NetworkViewModel by activityViewModels()

    private lateinit var mAuth: FirebaseAuth;

    private lateinit var email: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { status ->
            when (status.toString()) {
                "Available" -> {
                    mAuth = FirebaseAuth.getInstance()
                    val currentUser = mAuth.currentUser
                    if (currentUser != null) {
                        Log.d("login", "user $currentUser")
                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                    } else Log.d("login", "unregistered user")

                    binding.btnConfirm.setOnClickListener {
                        email = binding.etEmail.text.toString()
                        password = binding.etPassword.text.toString()
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(
                                context, "Enter valid details",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            signInUser(email, password)
                        }
                    }
                }
                else -> {
                    Snackbar.make(view, "Нет интернет-соединения", Snackbar.LENGTH_LONG).show()
                }
            }

        }
        binding.btnReg.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

    }

    private fun signInUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener(FragmentComponentManager.findActivity(requireView().context) as Activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context, "Вы вошли под именем $email",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                } else {
                    Toast.makeText(
                        context, "пользователь $email не зарегестрирован.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}