package com.hydrocare.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hydrocare.app.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabWeekly.setOnClickListener {
            binding.tabWeekly.setBackgroundResource(R.drawable.bg_segment_selected)
            binding.tabWeekly.setTextColor(requireContext().getColor(R.color.text_on_primary))
            binding.tabMonthly.setBackgroundResource(android.R.color.transparent)
            binding.tabMonthly.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.tvChartTitle.text = "This Week"
        }

        binding.tabMonthly.setOnClickListener {
            binding.tabMonthly.setBackgroundResource(R.drawable.bg_segment_selected)
            binding.tabMonthly.setTextColor(requireContext().getColor(R.color.text_on_primary))
            binding.tabWeekly.setBackgroundResource(android.R.color.transparent)
            binding.tabWeekly.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.tvChartTitle.text = "This Month"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
