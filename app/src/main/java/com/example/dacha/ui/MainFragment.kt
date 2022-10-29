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
import com.example.dacha.util.UiState
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal


@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()


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

        binding.numberPicker.apply {
            maxValue = temperature.size - 1
            minValue = 0
            wrapSelectorWheel = false
            value = maxValue / 2
            displayedValues = temperature
        }

        binding.customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.apply {
                    Log.d("heatingTemperature", heatingTemperature)
                    turnOnHeat(heatingTemperature)
                    //setRequireTemperature(heatingTemperature)
                    startCheckingStatus()
                }

                Log.d("heatingTemperature", heatingTemperature)
                binding.numberPicker.value = temperature.indexOf(heatingTemperature)
                binding.numberPicker.isEnabled = false
            } else {
                viewModel.turnOffHeat()
                viewModel.stopCheckingStatus()
                binding.tvRequiredTemp.text = ""
                binding.numberPicker.isEnabled = true
            }
        }
    }

    private var heatingTemperature = ""
    private var currentTemp: Float = 0f

    @SuppressLint("SetTextI18n")
    private fun observe() {
        viewModel.currentTemperature.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvTemp.text = ""
                    binding.animationView.visibility = View.VISIBLE

                }
                is UiState.Failure -> {
                    binding.tvTemp.text = state.error
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    binding.animationView.visibility = View.INVISIBLE

                }
                is UiState.Success -> {
                    binding.animationView.visibility = View.INVISIBLE
                    currentTemp = state.data
                    binding.circleIndicator.apply {
                        progressBarColorDirection = if (currentTemp < 0) {
                            CircularProgressBar.GradientDirection.BOTTOM_TO_END
                        } else CircularProgressBar.GradientDirection.TOP_TO_BOTTOM
                        startAngle = 0f
                        setProgressWithAnimation(currentTemp, 1000)
                    }
                    binding.tvTemp.text = String.format("%.1f", currentTemp)
                }
            }
        }

        viewModel.switcherStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is UiState.Loading -> {
                    binding.numberPicker.isEnabled = false
                    binding.aVPicker.visibility = View.VISIBLE
                }
                is UiState.Failure -> {
                    Toast.makeText(requireContext(), status.error, Toast.LENGTH_LONG).show()
                    binding.aVPicker.visibility = View.INVISIBLE
                }
                is UiState.Success -> {
                    heatingTemperature = status.data.second
                    Log.d("success","${status.data.second} vs $$heatingTemperature" )
                    binding.aVPicker.visibility = View.INVISIBLE
                    if (status.data.first == "1") {
                        binding.tvRequiredTemp.text = "${status.data.second} цель"
                        binding.customSwitch.isChecked = true
                        Log.d("pickerVal","${status.data.second} vs $heatingTemperature" )
                    } else {
                        binding.customSwitch.isChecked = false
                        binding.numberPicker.isEnabled = true
                        binding.numberPicker.setOnValueChangedListener { _, _, newVal ->
                            heatingTemperature = temperature[newVal]
                            Log.d("newVal", heatingTemperature)
                        }
                    }
                }
            }

        }
    }


    private val temperature = arrayOf(
        "18°C",
        "19°C",
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
        "32°C"
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}