package com.hydrocare.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hydrocare.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sliderInterval.addOnChangeListener { _, value, _ ->
            binding.tvIntervalValue.text = "${value.toInt()} min"
        }

        binding.btnGoal2000.setOnClickListener { selectGoalPreset(2000) }
        binding.btnGoal2500.setOnClickListener { selectGoalPreset(2500) }
        binding.btnGoal3000.setOnClickListener { selectGoalPreset(3000) }

        binding.btnStartTime.setOnClickListener { showTimePicker(isStart = true) }
        binding.btnEndTime.setOnClickListener { showTimePicker(isStart = false) }

        binding.btnSave.setOnClickListener {
            Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
        }

        binding.btnReset.setOnClickListener {
            Toast.makeText(requireContext(), "Today's intake reset.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectGoalPreset(goal: Int) {
        binding.etCustomGoal.setText("")
        Toast.makeText(requireContext(), "Goal set to $goal ml", Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(if (isStart) 7 else 22)
            .setMinute(0)
            .setTitleText(if (isStart) "Select Start Time" else "Select End Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val amPm = if (picker.hour < 12) "AM" else "PM"
            val hour = if (picker.hour % 12 == 0) 12 else picker.hour % 12
            val time = "%d:%02d %s".format(hour, picker.minute, amPm)
            if (isStart) binding.btnStartTime.text = time
            else binding.btnEndTime.text = time
        }

        picker.show(parentFragmentManager, "timePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
