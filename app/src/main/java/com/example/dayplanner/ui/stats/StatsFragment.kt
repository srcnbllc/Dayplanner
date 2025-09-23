package com.example.dayplanner.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.databinding.FragmentStatsBinding
import com.example.dayplanner.NoteDatabase
import com.example.dayplanner.finance.FinanceDao
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.ActivityLogViewModel
import com.example.dayplanner.ActivityLog
import java.text.NumberFormat
import java.util.*

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()
    private lateinit var financeDao: FinanceDao
    private lateinit var passwordDao: PasswordDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        val database = NoteDatabase.getDatabase(requireContext())
        financeDao = database.financeDao()
        passwordDao = database.passwordDao()

        setupRecyclerView()
        observeData()
        loadStats()
    }

    private fun setupRecyclerView() {
        val adapter = RecentActivityAdapter()
        binding.recentActivityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recentActivityRecyclerView.adapter = adapter
        
        // Mock recent activities
        val activities = listOf(
            RecentActivity("Yeni not eklendi", "Alışveriş listesi", "2 saat önce", android.R.drawable.ic_dialog_info),
            RecentActivity("İşlem eklendi", "Maaş - ₺15,000", "4 saat önce", android.R.drawable.ic_dialog_info),
            RecentActivity("Şifre güncellendi", "Gmail hesabı", "1 gün önce", android.R.drawable.ic_dialog_info),
            RecentActivity("Not tamamlandı", "Proje raporu", "2 gün önce", android.R.drawable.ic_dialog_info)
        )
        adapter.submitList(activities)
    }

    private fun observeData() {
        // Notes statistics
        viewModel.completedCount.observe(viewLifecycleOwner) { completed ->
            binding.completedCountText.text = completed.toString()
        }

        viewModel.streakDays.observe(viewLifecycleOwner) { streak ->
            binding.streakText.text = streak.toString()
        }

        viewModel.allNotesLiveData.observe(viewLifecycleOwner) { notes ->
            binding.notesCountText.text = notes.size.toString()
            binding.pinnedCountText.text = notes.count { it.isPinned }.toString()
        }

        // Finance statistics
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.totalIncomeText.text = formatter.format(income)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.totalExpenseText.text = formatter.format(expense)
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
            binding.balanceText.text = formatter.format(balance)
        }

        // Password statistics
        viewModel.passwordsCount.observe(viewLifecycleOwner) { count ->
            binding.passwordsCountText.text = count.toString()
        }

        viewModel.weakPasswordsCount.observe(viewLifecycleOwner) { count ->
            binding.weakPasswordsText.text = count.toString()
        }
    }

    private fun loadStats() {
        viewModel.loadAllStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class RecentActivity(
    val title: String,
    val description: String,
    val time: String,
    val icon: Int
)

class RecentActivityAdapter : androidx.recyclerview.widget.ListAdapter<RecentActivity, RecentActivityAdapter.ViewHolder>(RecentActivityDiffCallback()) {
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(com.example.dayplanner.R.layout.item_recent_activity, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: android.view.View) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        
        private val iconImage: android.widget.ImageView = itemView.findViewById(com.example.dayplanner.R.id.iconImage)
        private val titleText: android.widget.TextView = itemView.findViewById(com.example.dayplanner.R.id.titleText)
        private val descriptionText: android.widget.TextView = itemView.findViewById(com.example.dayplanner.R.id.descriptionText)
        private val timeText: android.widget.TextView = itemView.findViewById(com.example.dayplanner.R.id.timeText)
        
        fun bind(activity: RecentActivity) {
            titleText.text = activity.title
            descriptionText.text = activity.description
            timeText.text = activity.time
            iconImage.setImageResource(activity.icon)
        }
    }
    
    class RecentActivityDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<RecentActivity>() {
        override fun areItemsTheSame(oldItem: RecentActivity, newItem: RecentActivity): Boolean {
            return oldItem.title == newItem.title
        }
        
        override fun areContentsTheSame(oldItem: RecentActivity, newItem: RecentActivity): Boolean {
            return oldItem == newItem
        }
    }
}
