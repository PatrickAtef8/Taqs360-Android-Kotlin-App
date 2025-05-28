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
import com.example.taqs360.databinding.FragmentDatePickerBinding
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var _binding: FragmentDatePickerBinding? = null
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
        _binding = FragmentDatePickerBinding.inflate(themedInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            try {
                Toast.makeText(requireContext(), "Pick a date", Toast.LENGTH_SHORT).show()
                val calendar = Calendar.getInstance()
                val dpd = DatePickerDialog.newInstance(
                    this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                dpd.minDate = calendar
                dpd.accentColor = requireContext().getColor(R.color.navy_dark)
                dpd.show(childFragmentManager, "DatePickerDialog")
                Log.d("DatePickerFragment", "DatePickerDialog shown")
            } catch (e: Exception) {
                Log.e("DatePickerFragment", "Error showing DatePickerDialog", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        try {
            val localDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            val calendar = Calendar.getInstance().apply {
                time = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().let {
                    java.util.Date(it)
                }
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.setSelectedDate(calendar.timeInMillis)
            viewModel.onDateConfirmed()
            dismiss()
            Log.d("DatePickerFragment", "Date selected: $localDate")
        } catch (e: Exception) {
            Log.e("DatePickerFragment", "Error processing date", e)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
