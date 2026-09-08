package com.hydrocare.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.hydrocare.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentIntake = 1250
    private val dailyGoal = 2000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateProgress()

        binding.btnAdd250.setOnClickListener { addIntake(250) }
        binding.btnAdd500.setOnClickListener { addIntake(500) }
        binding.btnAddCustom.setOnClickListener { showCustomDialog() }
    }

    private fun addIntake(amount: Int) {
        currentIntake = minOf(currentIntake + amount, dailyGoal)
        updateProgress()
    }

    private fun updateProgress() {
        val percent = (currentIntake * 100) / dailyGoal
        binding.progressRing.progress = percent
        binding.tvCurrentIntake.text = currentIntake.toString()
        binding.tvPercentage.text = "$percent%"
        binding.tvRemaining.text = "${dailyGoal - currentIntake} ml remaining"
    }

    private fun showCustomDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Amount in ml"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Add Custom Amount")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toIntOrNull() ?: 0
                if (amount > 0) addIntake(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
