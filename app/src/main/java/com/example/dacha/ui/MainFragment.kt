package com.example.dacha.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.dacha.databinding.FragmentMainBinding
import com.example.dacha.util.SharedPrefConstants
import com.example.dacha.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal


@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()
    private lateinit var heatingTemperature: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
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
        viewModel.getCurrentTemperature()
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
            "30°C"
        )
        val isChecked = viewModel.localPrefs.getBoolean(
            SharedPrefConstants.SWITCHER_STATUS,
            false
        )
        binding.customSwitch.isChecked = isChecked
        if (isChecked) {
            val reqTemp = viewModel.localPrefs.getString(
                SharedPrefConstants.REQUIRED_TEMPERATURE,
                ""
            )
            binding.tvRequiredTemp.text = "$reqTemp цель"
            binding.numberPicker.apply {
                maxValue = temperature.size - 1
                minValue = 0
                value = temperature.indexOf(reqTemp)
                displayedValues = temperature
                heatingTemperature = reqTemp!!
                isEnabled = false
            }
        } else {
            heatingTemperature = temperature[(temperature.size - 1) / 2]
            binding.numberPicker.apply {
                maxValue = temperature.size - 1
                minValue = 0
                value = maxValue / 2
                wrapSelectorWheel = false
                displayedValues = temperature
            }
        }

        binding.numberPicker.setOnValueChangedListener { _, _, newVal ->
            heatingTemperature = temperature[newVal]
        }

        binding.customSwitch.setOnClickListener {
            if (binding.customSwitch.isChecked) {
                viewModel.apply {
                    turnOnHeat()
                    setRequireTemperature(heatingTemperature)
                }
                binding.tvRequiredTemp.text = "$heatingTemperature цель"
                binding.numberPicker.isEnabled = false
                viewModel.startCheckingStatus()

            } else {
                viewModel.turnOffHeat()
                binding.numberPicker.isEnabled = true
                viewModel.stopCheckingStatus()
            }
        }
    }

    private var currentTemp: Float = 0f

    private fun observe() {
        viewModel.currentTemperature.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvTemp.text = ""

                }
                is UiState.Failure -> {
                    binding.tvTemp.text = state.error
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()

                }
                is UiState.Success -> {
                    currentTemp = state.data
                    binding.circleIndicator.apply {
                        indeterminateMode = false
                        startAngle = 0f
                        setProgressWithAnimation(currentTemp, 1000)
                    }
                    binding.tvTemp.text = String.format("%.1f", currentTemp)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}