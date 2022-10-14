package com.example.dacha.ui

import android.annotation.SuppressLint
import android.os.Bundle
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            "30°C",
            "31°C",
            "32°C",
            "33°C",
            "34°C",
            "35°C"
        )
        var heatingTemperature = ""
        binding.numberPicker.apply {
            maxValue = temperature.size
            minValue = 1
            wrapSelectorWheel = false
            displayedValues = temperature
            setOnValueChangedListener { _, _, newVal ->
                heatingTemperature = temperature[newVal]
            }
        }

        val isChecked = viewModel.localPrefs.getBoolean(SharedPrefConstants.SWITCHER_STATUS, false)
        binding.customSwitch.isChecked = isChecked
        if (isChecked) {
            val reqTemp = viewModel.localPrefs.getString(
                SharedPrefConstants.REQUIRED_TEMPERATURE,
                ""
            )
            binding.numberPicker.apply {
                value = temperature.indexOf(reqTemp)
                isEnabled = false
            }
            binding.numberPicker.isEnabled = false
        }

        binding.customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.apply {
                    turnOnHeat()
                    setRequireTemperature(heatingTemperature)
                }
                binding.numberPicker.isEnabled = false
            } else {
                viewModel.turnOffHeat()
                binding.numberPicker.isEnabled = true
            }
        }

    }

    private fun observe() {
        viewModel.currentTemperature.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvTemp.text = ""
                    binding.paginationProgressBar.visibility = View.VISIBLE
                }
                is UiState.Failure -> {
                    binding.tvTemp.text = state.error
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()

                }
                is UiState.Success -> {
                    binding.tvTemp.text = state.data
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}