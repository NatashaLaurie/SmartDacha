package com.example.dacha.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dacha.databinding.FragmentMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


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
        switcherRef = Firebase.database.getReference("board1/outputs/digital/2")
        temperatureRef = Firebase.database.getReference("temperature")
        getTemperature()


        binding.customSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switcherRef.setValue(1)
            } else {
                switcherRef.setValue(0)
            }
        }

    }

    private fun getTemperature() {
        temperatureRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)
                val stringTemp = "$value °C"
                binding.tvTemp.text = stringTemp
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