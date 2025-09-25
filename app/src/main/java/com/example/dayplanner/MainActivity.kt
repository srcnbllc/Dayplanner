package com.example.dayplanner
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.setupWithNavController as setupToolbar
import com.example.dayplanner.databinding.ActivityMainBinding
import com.example.dayplanner.utils.CustomToast
import com.example.dayplanner.notifications.NotificationHelper
import com.example.dayplanner.theme.ThemeManager
import com.example.dayplanner.TrashManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "onCreate started")
        
        try {
            android.util.Log.d("MainActivity", "Inflating layout")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            android.util.Log.d("MainActivity", "Layout set")
            
            // Initialize notification channels (simplified)
            try {
                android.util.Log.d("MainActivity", "Initializing notification channels")
                NotificationHelper.initializeChannels(this)
                android.util.Log.d("MainActivity", "Notification channels initialized")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Notification channels error: ${e.message}")
            }
            
            // Apply theme (simplified)
            try {
                android.util.Log.d("MainActivity", "Applying theme")
                val themeManager = ThemeManager(this)
                themeManager.applyCurrentTheme()
                android.util.Log.d("MainActivity", "Theme applied")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Theme error: ${e.message}")
            }
            
            // Initialize trash cleanup
            try {
                android.util.Log.d("MainActivity", "Initializing trash cleanup")
                TrashManager.scheduleCleanup(this)
                android.util.Log.d("MainActivity", "Trash cleanup initialized")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Trash cleanup error: ${e.message}")
            }

            // Setup navigation (simplified)
            try {
                android.util.Log.d("MainActivity", "Setting up navigation")
                val navController = findNavController(R.id.nav_host_fragment)
                binding.bottomNavigation.setupWithNavController(navController)
                android.util.Log.d("MainActivity", "Navigation setup complete")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Navigation error: ${e.message}")
            }
            
            // Setup toolbar (simplified)
            try {
                android.util.Log.d("MainActivity", "Setting up toolbar")
                setSupportActionBar(binding.topAppBar)
                binding.topAppBar.inflateMenu(R.menu.top_app_bar_menu)
                android.util.Log.d("MainActivity", "Toolbar setup complete")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Toolbar error: ${e.message}")
            }
            
            
            binding.topAppBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add -> {
                        startActivity(Intent(this, AddNoteActivity::class.java))
                        true
                    }
                    R.id.filter_new -> {
                        supportFragmentManager.setFragmentResult("filter_status", bundleOf("status" to "NEW"))
                        true
                    }
                    R.id.filter_notes -> {
                        supportFragmentManager.setFragmentResult("filter_status", bundleOf("status" to "NOTES"))
                        true
                    }
                    R.id.filter_deleted -> {
                        supportFragmentManager.setFragmentResult("filter_status", bundleOf("status" to "DELETED"))
                        true
                    }
                    R.id.action_settings -> {
                        try {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Settings activity error: ${e.message}")
                        }
                        true
                    }
                    else -> false
                }
            }
            
            android.util.Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            finish()
        }
    }
}
