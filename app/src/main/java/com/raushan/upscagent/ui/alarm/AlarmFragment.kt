package com.raushan.upscagent.ui.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.raushan.upscagent.R
import com.raushan.upscagent.data.model.StudyAlarm
import com.raushan.upscagent.data.repository.AppRepository
import com.raushan.upscagent.utils.AlarmHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlarmFragment : Fragment() {

    private lateinit var repository: AppRepository
    private lateinit var adapter: AlarmAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepository(requireContext())

        adapter = AlarmAdapter(
            onToggle = { alarm, enabled -> toggleAlarm(alarm, enabled) },
            onDelete = { alarm -> deleteAlarm(alarm) },
            onEdit = { alarm -> showAddAlarmDialog(alarm) }
        )

        view.findViewById<RecyclerView>(R.id.rv_alarms).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlarmFragment.adapter
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_alarm).setOnClickListener {
            showAddAlarmDialog(null)
        }

        // Observe alarms
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllAlarms().collectLatest { alarms ->
                adapter.submitList(alarms)
                view.findViewById<TextView>(R.id.tv_empty_alarm)?.visibility =
                    if (alarms.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showAddAlarmDialog(existingAlarm: StudyAlarm?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_alarm, null)

        val etSubject = dialogView.findViewById<EditText>(R.id.et_alarm_subject)
        val tvStartTime = dialogView.findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = dialogView.findViewById<TextView>(R.id.tv_end_time)
        val etNotes = dialogView.findViewById<EditText>(R.id.et_alarm_notes)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_days)

        var startHour = existingAlarm?.startHour ?: 7
        var startMinute = existingAlarm?.startMinute ?: 0
        var endHour = existingAlarm?.endHour ?: 8
        var endMinute = existingAlarm?.endMinute ?: 0

        // Subjects dropdown
        val subjects = arrayOf("Physics", "Chemistry", "Mathematics", "Geography", "History",
            "Polity", "Economy", "Science & Tech", "Environment", "Art & Culture",
            "Ethics", "Current Affairs", "CSAT", "Essay", "General Studies", "Optional")

        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.et_alarm_subject)
        autoComplete?.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects))

        // Pre-fill if editing
        existingAlarm?.let {
            etSubject.setText(it.subject)
            etNotes.setText(it.notes)
            // Set day chips
            val enabledDays = it.daysOfWeek.split(",").map { d -> d.trim().toInt() }
            for (idx in 0 until chipGroup.childCount) {
                (chipGroup.getChildAt(idx) as? Chip)?.isChecked = enabledDays.contains(idx + 1)
            }
        }

        tvStartTime.text = AlarmHelper.formatTime(startHour, startMinute)
        tvEndTime.text = AlarmHelper.formatTime(endHour, endMinute)

        tvStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                startHour = h; startMinute = m
                tvStartTime.text = AlarmHelper.formatTime(h, m)
            }, startHour, startMinute, false).show()
        }

        tvEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                endHour = h; endMinute = m
                tvEndTime.text = AlarmHelper.formatTime(h, m)
            }, endHour, endMinute, false).show()
        }

        val title = if (existingAlarm != null) "Edit Study Alarm" else "Add Study Alarm"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val subject = etSubject.text.toString().trim()
                if (subject.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a subject", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Get selected days
                val selectedDays = mutableListOf<Int>()
                for (idx in 0 until chipGroup.childCount) {
                    if ((chipGroup.getChildAt(idx) as? Chip)?.isChecked == true) {
                        selectedDays.add(idx + 1)
                    }
                }
                if (selectedDays.isEmpty()) selectedDays.addAll(1..7)

                val alarm = StudyAlarm(
                    id = existingAlarm?.id ?: 0,
                    subject = subject,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    daysOfWeek = selectedDays.joinToString(","),
                    notes = etNotes.text.toString().trim(),
                    isEnabled = true
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    val id = repository.insertAlarm(alarm)
                    val savedAlarm = alarm.copy(id = id.toInt())
                    AlarmHelper.scheduleAlarm(requireContext(), savedAlarm)
                    Toast.makeText(requireContext(),
                        "Alarm set: $subject (${AlarmHelper.formatTime(startHour, startMinute)} - ${AlarmHelper.formatTime(endHour, endMinute)})",
                        Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleAlarm(alarm: StudyAlarm, enabled: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.toggleAlarm(alarm.id, enabled)
            if (enabled) {
                AlarmHelper.scheduleAlarm(requireContext(), alarm.copy(isEnabled = true))
            } else {
                AlarmHelper.cancelAlarm(requireContext(), alarm)
            }
        }
    }

    private fun deleteAlarm(alarm: StudyAlarm) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Alarm")
            .setMessage("Remove ${alarm.subject} alarm?")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    AlarmHelper.cancelAlarm(requireContext(), alarm)
                    repository.deleteAlarm(alarm)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

// ==================== ALARM ADAPTER ====================

class AlarmAdapter(
    private val onToggle: (StudyAlarm, Boolean) -> Unit,
    private val onDelete: (StudyAlarm) -> Unit,
    private val onEdit: (StudyAlarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private var alarms = listOf<StudyAlarm>()

    fun submitList(list: List<StudyAlarm>) {
        alarms = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(alarms[position])
    }

    override fun getItemCount() = alarms.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubject: TextView = view.findViewById(R.id.tv_alarm_subject)
        private val tvTime: TextView = view.findViewById(R.id.tv_alarm_time)
        private val tvDays: TextView = view.findViewById(R.id.tv_alarm_days)
        private val switchAlarm: SwitchMaterial = view.findViewById(R.id.switch_alarm)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_alarm)

        fun bind(alarm: StudyAlarm) {
            tvSubject.text = alarm.subject
            tvTime.text = "${AlarmHelper.formatTime(alarm.startHour, alarm.startMinute)} → ${AlarmHelper.formatTime(alarm.endHour, alarm.endMinute)}"

            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val enabledDays = alarm.daysOfWeek.split(",").map { it.trim().toInt() }
            tvDays.text = enabledDays.map { dayNames.getOrElse(it - 1) { "" } }.joinToString(", ")

            switchAlarm.isChecked = alarm.isEnabled
            switchAlarm.setOnCheckedChangeListener { _, checked -> onToggle(alarm, checked) }
            btnDelete.setOnClickListener { onDelete(alarm) }
            itemView.setOnClickListener { onEdit(alarm) }
        }
    }
}
