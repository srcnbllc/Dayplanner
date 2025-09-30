package com.example.dayplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dayplanner.databinding.ActivityMainBinding
import android.content.pm.ShortcutManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val noteViewModel: NoteViewModel by viewModels() // ViewModel'i kullanıyoruz
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Quick Actions (Shortcuts) ekle
            setupQuickActions()

            // NoteAdapter'ı RecyclerView'a bağlamak için oluşturduk
            noteAdapter = NoteAdapter(
                onClick = { note ->
                    // Herhangi bir not'a tıklandığında notu düzenlemek için AddNoteActivity'ye geçiş yapıyoruz
                    val intent = Intent(this, AddNoteActivity::class.java)
                    intent.putExtra("noteId", note.id)
                    startActivity(intent)
                },
                onLockToggle = { note, shouldLock ->
                    if (shouldLock) {
                        // Kilitle
                        val updated = note.copy(isEncrypted = true, isLocked = true)
                        noteViewModel.update(updated)
                    } else {
                        // Kilidi kaldır
                        val updated = note.copy(isEncrypted = false, isLocked = false)
                        noteViewModel.update(updated)
                    }
                },
                onSoftDelete = { note ->
                    noteViewModel.softDeleteById(note.id)
                },
                onPinToggle = { note, shouldPin ->
                    noteViewModel.setPinned(note.id, shouldPin)
                }
            )

            // Navigation setup
            setupNavigation()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            // Fallback: Show error message and finish
            android.widget.Toast.makeText(this, "Uygulama başlatılamadı: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
    
    private fun setupQuickActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            
            val shortcuts = listOf(
                ShortcutInfo.Builder(this, "quick_add_note")
                    .setShortLabel("Hızlı Not")
                    .setLongLabel("Yeni Not Ekle")
                    .setIcon(Icon.createWithResource(this, android.R.drawable.ic_input_add))
                    .setIntent(Intent(this, AddNoteActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                    })
                    .build(),
                    
                ShortcutInfo.Builder(this, "quick_add_finance")
                    .setShortLabel("Hızlı Finans")
                    .setLongLabel("Finans Kaydı Ekle")
                    .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_edit))
                    .setIntent(Intent(this, AddNoteActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("type", "finance")
                    })
                    .build(),
                    
                ShortcutInfo.Builder(this, "quick_add_password")
                    .setShortLabel("Hızlı Şifre")
                    .setLongLabel("Şifre Kaydı Ekle")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_lock))
                    .setIntent(Intent(this, AddNoteActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        putExtra("type", "password")
                    })
                    .build()
            )
            
            shortcutManager.dynamicShortcuts = shortcuts
        }
    }
    
    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            // Bottom navigation ile NavController'ı bağla
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in setupNavigation: ${e.message}", e)
        }
    }
}
