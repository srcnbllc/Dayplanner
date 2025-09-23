package com.example.dayplanner.ui.passwords

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.example.dayplanner.PasswordAdapter
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentPasswordsBinding
import com.example.dayplanner.passwords.Password
import com.example.dayplanner.passwords.PasswordDao
import com.example.dayplanner.NoteDatabase
import java.security.SecureRandom

class PasswordsFragment : Fragment() {

    private var _binding: FragmentPasswordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var passwordDao: PasswordDao
    private lateinit var passwordAdapter: PasswordAdapter
    private val viewModel: PasswordsViewModel by viewModels {
        PasswordsViewModelFactory(passwordDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        val database = NoteDatabase.getDatabase(requireContext())
        passwordDao = database.passwordDao()

        setupRecyclerView()
        setupAddButton()
        setupSearch()
        setupCategorySpinner()
        observeData()
    }

    private fun setupRecyclerView() {
        passwordAdapter = PasswordAdapter(
            onClick = { password ->
                showPasswordDetails(password)
            },
            onCopy = { password ->
                copyPasswordToClipboard(password)
            },
            onView = { password ->
                showPasswordDialog(password)
            }
        )

        binding.passwordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.passwordsRecyclerView.adapter = passwordAdapter

        // Swipe to delete
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.bindingAdapterPosition
                val password = passwordAdapter.currentList.getOrNull(position) ?: return
                showDeleteConfirmation(password)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.passwordsRecyclerView)
    }

    private fun setupAddButton() {
        binding.addPasswordButton.setOnClickListener {
            showAddPasswordDialog()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            viewModel.searchPasswords(query)
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf("Tümü")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryList = mutableListOf("Tümü")
            categoryList.addAll(categories)
            adapter.clear()
            adapter.addAll(categoryList)
            adapter.notifyDataSetChanged()
        }

        binding.categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as? String ?: "Tümü"
                viewModel.getPasswordsByCategory(selectedCategory)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeData() {
        viewModel.passwords.observe(viewLifecycleOwner) { passwords ->
            passwordAdapter.submitList(passwords)
            binding.passwordsCountText.text = passwords.size.toString()
        }
    }

    private fun showAddPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_password, null)

        val titleEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleEditText)
        val usernameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordEditText)
        val categorySpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.categorySpinner)
        val websiteEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.websiteEditText)
        val notesEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.notesEditText)
        val generatePasswordButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.generatePasswordButton)

        // Setup category spinner
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Sosyal Medya", "E-posta", "Banka", "İş", "E-ticaret", "Diğer"))
        categorySpinner.setAdapter(categoryAdapter)

        // Generate password button
        generatePasswordButton.setOnClickListener {
            val generatedPassword = generateSecurePassword()
            passwordEditText.setText(generatedPassword)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Ekle") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val username = usernameEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                val category = categorySpinner.text.toString().trim()
                val website = websiteEditText.text.toString().trim()
                val notes = notesEditText.text.toString().trim()

                if (title.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && category.isNotEmpty()) {
                    val passwordObj = Password(
                        id = 0,
                        title = title,
                        username = username,
                        password = password,
                        category = category,
                        website = website.ifEmpty { null },
                        notes = notes.ifEmpty { null }
                    )
                    viewModel.addPassword(passwordObj)
                    Toast.makeText(requireContext(), "Şifre eklendi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gerekli alanları doldurun", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showPasswordDetails(password: Password) {
        val message = buildString {
            append("Kullanıcı Adı: ${password.username}\n")
            append("Kategori: ${password.category}\n")
            if (!password.website.isNullOrEmpty()) {
                append("Website: ${password.website}\n")
            }
            if (!password.notes.isNullOrEmpty()) {
                append("Notlar: ${password.notes}\n")
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(password.title)
            .setMessage(message)
            .setPositiveButton("Şifreyi Kopyala") { _, _ ->
                copyPasswordToClipboard(password)
            }
            .setNeutralButton("Düzenle") { _, _ ->
                showEditPasswordDialog(password)
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun showPasswordDialog(password: Password) {
        val decryptedPassword = viewModel.decryptPassword(password.password)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Şifre")
            .setMessage(decryptedPassword)
            .setPositiveButton("Kopyala") { _, _ ->
                copyPasswordToClipboard(password)
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun showEditPasswordDialog(password: Password) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_password, null)

        val titleEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleEditText)
        val usernameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordEditText)
        val categorySpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.categorySpinner)
        val websiteEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.websiteEditText)
        val notesEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.notesEditText)
        val generatePasswordButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.generatePasswordButton)

        // Pre-fill fields
        titleEditText.setText(password.title)
        usernameEditText.setText(password.username)
        passwordEditText.setText(viewModel.decryptPassword(password.password))
        categorySpinner.setText(password.category)
        websiteEditText.setText(password.website ?: "")
        notesEditText.setText(password.notes ?: "")

        // Setup category spinner
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, 
            listOf("Sosyal Medya", "E-posta", "Banka", "İş", "E-ticaret", "Diğer"))
        categorySpinner.setAdapter(categoryAdapter)

        // Generate password button
        generatePasswordButton.setOnClickListener {
            val generatedPassword = generateSecurePassword()
            passwordEditText.setText(generatedPassword)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Güncelle") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val username = usernameEditText.text.toString().trim()
                val passwordText = passwordEditText.text.toString().trim()
                val category = categorySpinner.text.toString().trim()
                val website = websiteEditText.text.toString().trim()
                val notes = notesEditText.text.toString().trim()

                if (title.isNotEmpty() && username.isNotEmpty() && passwordText.isNotEmpty() && category.isNotEmpty()) {
                    val updatedPassword = password.copy(
                        title = title,
                        username = username,
                        password = passwordText,
                        category = category,
                        website = website.ifEmpty { null },
                        notes = notes.ifEmpty { null }
                    )
                    viewModel.updatePassword(updatedPassword)
                    Toast.makeText(requireContext(), "Şifre güncellendi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gerekli alanları doldurun", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun copyPasswordToClipboard(password: Password) {
        val decryptedPassword = viewModel.decryptPassword(password.password)
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Password", decryptedPassword)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Şifre panoya kopyalandı", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation(password: Password) {
        AlertDialog.Builder(requireContext())
            .setTitle("Şifreyi Sil")
            .setMessage("Bu şifreyi silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                viewModel.deletePassword(password)
                Toast.makeText(requireContext(), "Şifre silindi", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("İptal") { _, _ ->
                // Refresh the adapter to restore the item
                passwordAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun generateSecurePassword(): String {
        val length = 16
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..length)
            .map { chars[SecureRandom().nextInt(chars.length)] }
            .joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


