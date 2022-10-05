package com.example.dacha.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dacha.R
import com.example.dacha.databinding.FragmentMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var switcherRef: DatabaseReference
    private lateinit var temperatureRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        NoInternetDialogSignal.Builder(
            requireActivity(),
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        showProgressBar()
                        val temperature = arrayOf(
                            "20°C",
                            "21°C",
                            "22°C",
                            "23°C",
                            "24°C",
                            "25°C",
                            "26°C",
                            "27°C",
                            "28°C",
                            "29°C",
                            "30°C",
                            "31°C",
                            "32°C",
                            "33°C",
                            "34°C",
                            "35°C"
                        )
                        binding.numberPicker.apply {
                            maxValue = temperature.size - 1
                            minValue = 0
                            wrapSelectorWheel = false
                            displayedValues = temperature
                        }


                        switcherRef = Firebase.database.getReference("board1/outputs/digital/2")
                        temperatureRef = Firebase.database.getReference("temperature")

                        getTemperature()

                        binding.customSwitch.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                switcherRef.setValue(1)
                            } else {
                                switcherRef.setValue(0)
                            }
                        }

                        binding.btnBack.setOnClickListener {
                            Firebase.auth.signOut()
                            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                        }
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "Нет интернета" // Optional
                noInternetConnectionMessage =
                    "Проверьте интернет-подключение и попробуйте снова." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Плиз включите" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Моб. данные" // Optional

                onAirplaneModeTitle = "Нет интернета" // Optional
                onAirplaneModeMessage = "Вы включили режим полёта." // Optional
                pleaseTurnOffText = "Плиз отключите" // Optional
                airplaneModeOffButtonText = "Режим полёта" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()


    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun getTemperature() {
        temperatureRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)
                val stringTemp = "$value °C"
                binding.tvTemp.text = stringTemp
                hideProgressBar()
                Log.d("mainFragment", "$value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("mainFragment", "$error")
                Toast.makeText(context, "Ошибка при получении данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}