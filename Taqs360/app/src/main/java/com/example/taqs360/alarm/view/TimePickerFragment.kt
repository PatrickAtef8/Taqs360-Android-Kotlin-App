package com.example.taqs360.alarm.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.taqs360.R
import com.example.taqs360.alarm.viewmodel.AlarmViewModel
import com.example.taqs360.databinding.FragmentTimePickerBinding
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.Calendar

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private var _binding: FragmentTimePickerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val themedInflater = inflater.cloneInContext(requireContext())
        _binding = FragmentTimePickerBinding.inflate(themedInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            try {
                Toast.makeText(requireContext(), "Pick a Time", Toast.LENGTH_SHORT).show()
                val calendar = Calendar.getInstance()
                val tpd = TimePickerDialog.newInstance(
                    this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // AM/PM mode
                )
                tpd.enableSeconds(false)
                tpd.accentColor = requireContext().getColor(R.color.navy_dark)
                tpd.show(childFragmentManager, "TimePickerDialog") // Use childFragmentManager for nested fragments
                Log.d("TimePickerFragment", "TimePickerDialog shown")
            } catch (e: Exception) {
                Log.e("TimePickerFragment", "Error showing TimePickerDialog", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        try {
            viewModel.setSelectedTime(hourOfDay, minute)
            viewModel.onTimeConfirmed()
            dismiss()
            Log.d("TimePickerFragment", "Time selected: $hourOfDay:$minute")
        } catch (e: Exception) {
            Log.e("TimePickerFragment", "Error processing time", e)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, // Increase width to ensure buttons are visible
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
