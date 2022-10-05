package com.example.dacha.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dacha.NetworkViewModel
import com.example.dacha.R
import com.example.dacha.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NetworkViewModel by activityViewModels()

    private lateinit var mAuth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            Log.d("login", "user $currentUser")
        } else Log.d("login", "unregistered user")
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
                                context, "Введите корректные данные",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            signUpUser(email, password)
                        }
                    }
                }
                else -> {
                    binding.btnConfirm.setOnClickListener {
                        Toast.makeText(
                            context, "Нет интернет-соединения",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }


        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun signUpUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        context, "Регистрация прошла успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        context, "Ошибка",
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