// AddNoteActivity.kt dosyasına eklenecek kod

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {
    
    private lateinit var reminderSwitch: SwitchMaterial
    private lateinit var reminderDetailsLayout: LinearLayout
    private lateinit var datePickerButton: MaterialButton
    private lateinit var timePickerButton: MaterialButton
    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var selectedPeriodText: TextView
    private lateinit var reminderPeriodChipGroup: ChipGroup
    
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    private var selectedPeriod: String = "Tek Sefer"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        
        initializeViews()
        setupReminderFunctionality()
    }
    
    private fun initializeViews() {
        reminderSwitch = findViewById(R.id.reminderSwitch)
        reminderDetailsLayout = findViewById(R.id.reminderDetailsLayout)
        datePickerButton = findViewById(R.id.datePickerButton)
        timePickerButton = findViewById(R.id.timePickerButton)
        selectedDateText = findViewById(R.id.selectedDateText)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        selectedPeriodText = findViewById(R.id.selectedPeriodText)
        reminderPeriodChipGroup = findViewById(R.id.reminderPeriodChipGroup)
    }
    
    private fun setupReminderFunctionality() {
        // Reminder Switch Toggle
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                reminderDetailsLayout.visibility = View.VISIBLE
                // Smooth animation
                reminderDetailsLayout.alpha = 0f
                reminderDetailsLayout.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            } else {
                reminderDetailsLayout.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        reminderDetailsLayout.visibility = View.GONE
                    }
                    .start()
            }
        }
        
        // Date Picker
        datePickerButton.setOnClickListener {
            showDatePicker()
        }
        
        // Time Picker
        timePickerButton.setOnClickListener {
            showTimePicker()
        }
        
        // Reminder Period Selection
        reminderPeriodChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = group.findViewById<Chip>(checkedIds.first())
            selectedPeriod = selectedChip?.text?.toString() ?: "Tek Sefer"
            selectedPeriodText.text = selectedPeriod
            updateReminderDisplay()
        }
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
                updateReminderDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        // Minimum date as today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
                updateReminderDisplay()
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            true // 24 hour format
        )
        timePickerDialog.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
        selectedDateText.text = dateFormat.format(selectedDate.time)
        datePickerButton.text = "Tarih: ${dateFormat.format(selectedDate.time)}"
    }
    
    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        selectedTimeText.text = timeFormat.format(selectedTime.time)
        timePickerButton.text = "Saat: ${timeFormat.format(selectedTime.time)}"
    }
    
    private fun updateReminderDisplay() {
        if (reminderSwitch.isChecked) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            selectedDateText.text = dateFormat.format(selectedDate.time)
            selectedTimeText.text = timeFormat.format(selectedTime.time)
            selectedPeriodText.text = selectedPeriod
            
            // Show success message
            Toast.makeText(this, "Hatırlatma ayarlandı: ${dateFormat.format(selectedDate.time)} ${timeFormat.format(selectedTime.time)} ($selectedPeriod)", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Save note with reminder
    private fun saveNoteWithReminder() {
        if (reminderSwitch.isChecked) {
            val reminderDateTime = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }
            
            // Create reminder
            createReminder(reminderDateTime, selectedPeriod)
        }
        
        // Save note
        saveNote()
    }
    
    private fun createReminder(dateTime: Calendar, period: String) {
        // TODO: Implement actual reminder creation
        // This could use AlarmManager, WorkManager, or a notification system
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("tr"))
        Toast.makeText(this, "Hatırlatma oluşturuldu: ${dateFormat.format(dateTime.time)} ($period)", Toast.LENGTH_LONG).show()
    }
    
    private fun saveNote() {
        // TODO: Implement note saving logic
        Toast.makeText(this, "Not kaydedildi!", Toast.LENGTH_SHORT).show()
    }
}

