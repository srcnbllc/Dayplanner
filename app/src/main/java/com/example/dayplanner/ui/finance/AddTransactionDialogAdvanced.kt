package com.example.dayplanner.ui.finance

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import com.example.dayplanner.utils.CustomToast
import androidx.fragment.app.DialogFragment
import com.example.dayplanner.R
import com.example.dayplanner.finance.*
import com.example.dayplanner.finance.IncomeSubtype
import com.example.dayplanner.finance.ExpenseSubtype
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionDialogAdvanced : DialogFragment() {

    // Core fields
    private lateinit var transactionTypeSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var subtypeSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var currencySpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var categorySpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var dateEditText: TextInputEditText
    private lateinit var recurringSwitch: SwitchMaterial
    private lateinit var recurringIntervalLayout: TextInputLayout
    private lateinit var recurringIntervalSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var reminderSwitch: SwitchMaterial
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var dynamicFieldsContainer: LinearLayout
    private lateinit var backButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    // Dynamic fields for different transaction types
    private val dynamicFields = mutableMapOf<String, TextInputEditText>()
    private var selectedTransactionType: TransactionType = TransactionType.EXPENSE
    private var selectedSubtype: String = ""

    private var onTransactionSaved: ((Transaction) -> Unit)? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        fun newInstance(onTransactionSaved: (Transaction) -> Unit): AddTransactionDialogAdvanced {
            return AddTransactionDialogAdvanced().apply {
                this.onTransactionSaved = onTransactionSaved
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_transaction_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupSpinners()
        setupListeners()
        setupDynamicFields()
    }

    private fun setupViews(view: View) {
        transactionTypeSpinner = view.findViewById(R.id.transactionTypeSpinner)
        subtypeSpinner = view.findViewById(R.id.subtypeSpinner)
        titleEditText = view.findViewById(R.id.titleEditText)
        amountEditText = view.findViewById(R.id.amountEditText)
        currencySpinner = view.findViewById(R.id.currencySpinner)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        dateEditText = view.findViewById(R.id.dateEditText)
        recurringSwitch = view.findViewById(R.id.recurringSwitch)
        recurringIntervalLayout = view.findViewById(R.id.recurringIntervalLayout)
        recurringIntervalSpinner = view.findViewById(R.id.recurringIntervalSpinner)
        reminderSwitch = view.findViewById(R.id.reminderSwitch)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        dynamicFieldsContainer = view.findViewById(R.id.dynamicFieldsContainer)
        backButton = view.findViewById(R.id.backButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Set current date
        dateEditText.setText(dateFormat.format(Date(selectedDate)))
    }

    private fun setupSpinners() {
        // Transaction Type
        val transactionTypes = listOf("Gelir", "Gider")
        val transactionTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, transactionTypes)
        transactionTypeSpinner.setAdapter(transactionTypeAdapter)

        // Currency
        val currencies = listOf("TRY", "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD")
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        currencySpinner.setAdapter(currencyAdapter)

        // Recurring Interval
        val intervals = listOf("Günlük", "Haftalık", "Aylık", "Yıllık")
        val intervalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, intervals)
        recurringIntervalSpinner.setAdapter(intervalAdapter)
    }

    private fun setupListeners() {
        // Transaction Type Change
        transactionTypeSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedTransactionType = if (position == 0) TransactionType.INCOME else TransactionType.EXPENSE
            updateSubtypes(selectedTransactionType)
            updateCategories(selectedTransactionType)
            setupDynamicFields()
        }

        // Subtype Change
        subtypeSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedSubtype = subtypeSpinner.text.toString()
            setupDynamicFields()
        }

        // Date Picker
        dateEditText.setOnClickListener {
            showDatePicker()
        }

        // Recurring Switch
        recurringSwitch.setOnCheckedChangeListener { _, isChecked ->
            recurringIntervalLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Back Button
        backButton.setOnClickListener {
            dismiss()
        }

        // Cancel Button
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Save Button
        saveButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateSubtypes(transactionType: TransactionType) {
        val subtypes = when (transactionType) {
            TransactionType.INCOME -> IncomeSubtype.values().map { getIncomeSubtypeDisplayName(it) }
            TransactionType.EXPENSE -> ExpenseSubtype.values().map { getExpenseSubtypeDisplayName(it) }
        }
        val subtypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subtypes)
        subtypeSpinner.setAdapter(subtypeAdapter)
    }

    private fun updateCategories(transactionType: TransactionType) {
        val categories = when (transactionType) {
            TransactionType.INCOME -> listOf("Maaş", "Serbest Gelir", "Kira Geliri", "Yatırım", "Alacak", "Prim/Bonus", "Emekli Maaşı", "Faiz/Temettü", "Diğer")
            TransactionType.EXPENSE -> listOf("Kira Gideri", "Banka Kredisi", "Fatura Ödemeleri", "Sigorta", "Vergi/Harç", "Eğitim", "Sağlık", "Ulaşım", "Gıda/Market", "Alışveriş", "Tatil/Seyahat", "Abonelikler", "Diğer")
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categorySpinner.setAdapter(categoryAdapter)
    }

    private fun setupDynamicFields() {
        dynamicFieldsContainer.removeAllViews()
        dynamicFields.clear()
        
        selectedSubtype = subtypeSpinner.text.toString()
        
        when (selectedTransactionType) {
            TransactionType.INCOME -> setupIncomeFields()
            TransactionType.EXPENSE -> setupExpenseFields()
        }
    }

    private fun setupIncomeFields() {
        when (selectedSubtype) {
            "Maaş" -> {
                addDynamicField("İşveren", "İşveren adını girin")
                addDynamicField("Net Tutar", "Net maaş tutarını girin")
                addDynamicField("Gross Tutar", "Gross maaş tutarını girin")
                addDynamicField("Ödeme Günü", "Ödeme gününü girin")
                addDynamicField("Banka Hesabı", "Banka hesap bilgilerini girin")
                addDynamicField("SSK/SGK Kesintisi", "Kesinti tutarını girin")
            }
            "Serbest Gelir" -> {
                addDynamicField("Fatura No", "Fatura numarasını girin")
                addDynamicField("Müşteri", "Müşteri adını girin")
                addDynamicField("KDV", "KDV tutarını girin")
                addDynamicField("Ödeme Şekli", "Ödeme şeklini girin")
            }
            "Kira Geliri" -> {
                addDynamicField("Sözleşme Tarihi", "Sözleşme tarihini girin")
                addDynamicField("Süre", "Sözleşme süresini girin")
                addDynamicField("Aylık Tutar", "Aylık kira tutarını girin")
                addDynamicField("Ödeme Günü", "Ödeme gününü girin")
                addDynamicField("Banka", "Banka bilgilerini girin")
            }
            "Yatırım Getirisi" -> {
                addDynamicField("Tür", "Hisse, Tahvil, Mevduat, Kripto")
                addDynamicField("Anapara", "Anapara tutarını girin")
                addDynamicField("Faiz/Kâr Oranı", "Faiz veya kâr oranını girin")
                addDynamicField("Periyot", "Yatırım periyodunu girin")
                addDynamicField("Vade", "Vade tarihini girin")
            }
            "Alacak" -> {
                addDynamicField("Kişi/Kurum", "Alacaklı kişi veya kurumu girin")
                addDynamicField("Tahsil Tarihi", "Tahsil edilecek tarihi girin")
                addDynamicField("Hatırlatma", "Hatırlatma tarihini girin")
            }
            "Prim/Bonus" -> {
                addDynamicField("Kaynak", "Prim/bonus kaynağını girin")
                addDynamicField("Tarih", "Prim/bonus tarihini girin")
            }
            "Emekli Maaşı" -> {
                addDynamicField("Kurum", "Emekli sandığı kurumunu girin")
                addDynamicField("Tarih", "Ödeme tarihini girin")
                addDynamicField("Banka", "Banka bilgilerini girin")
            }
            "Faiz/Temettü" -> {
                addDynamicField("Tür", "Faiz veya temettü türünü girin")
                addDynamicField("Oran", "Faiz veya temettü oranını girin")
                addDynamicField("Vade", "Vade tarihini girin")
            }
            "Diğer Gelirler" -> {
                addDynamicField("Kaynak", "Gelir kaynağını girin")
                addDynamicField("Açıklama", "Detaylı açıklama girin")
            }
        }
    }

    private fun setupExpenseFields() {
        when (selectedSubtype) {
            "Kira Gideri" -> {
                addDynamicField("Sözleşme", "Sözleşme bilgilerini girin")
                addDynamicField("Süre", "Sözleşme süresini girin")
                addDynamicField("Ödeme Planı", "Ödeme planını girin")
                addDynamicField("Vade", "Vade tarihini girin")
                addDynamicField("Kapora", "Kapora tutarını girin")
                addDynamicField("Banka", "Banka bilgilerini girin")
                addDynamicField("Hatırlatma", "Hatırlatma tarihini girin")
            }
            "Banka Kredisi" -> {
                addDynamicField("Kredi Tutarı", "Toplam kredi tutarını girin")
                addDynamicField("Faiz Oranı", "Faiz oranını girin")
                addDynamicField("Taksit Sayısı", "Toplam taksit sayısını girin")
                addDynamicField("Aylık Tutar", "Aylık ödeme tutarını girin")
                addDynamicField("Son Ödeme Tarihi", "Son ödeme tarihini girin")
            }
            "Fatura Ödemeleri" -> {
                addDynamicField("Tür", "Elektrik, Su, Doğalgaz, İnternet, Telefon, TV")
                addDynamicField("Abone No", "Abone numarasını girin")
                addDynamicField("Dönem", "Fatura dönemini girin")
                addDynamicField("Son Tarih", "Son ödeme tarihini girin")
                addDynamicField("Otomatik Ödeme", "Otomatik ödeme durumunu girin")
            }
            "Sigorta" -> {
                addDynamicField("Tür", "Sağlık, Araç, Konut, Hayat")
                addDynamicField("Poliçe No", "Poliçe numarasını girin")
                addDynamicField("Prim Tutarı", "Prim tutarını girin")
                addDynamicField("Periyot", "Ödeme periyodunu girin")
                addDynamicField("Şirket", "Sigorta şirketini girin")
                addDynamicField("Son Tarih", "Son ödeme tarihini girin")
            }
            "Vergi/Harç" -> {
                addDynamicField("Tür", "Gelir, KDV, MTV, Tapu")
                addDynamicField("Dönem", "Vergi dönemini girin")
                addDynamicField("Ödeme Tarihi", "Ödeme tarihini girin")
            }
            "Eğitim" -> {
                addDynamicField("Kurum", "Eğitim kurumunu girin")
                addDynamicField("Eğitim Türü", "Okul, Kurs, vb.")
                addDynamicField("Fatura No", "Fatura numarasını girin")
                addDynamicField("Ödeme Tarihi", "Ödeme tarihini girin")
            }
            "Sağlık" -> {
                addDynamicField("Kurum/Hastane", "Sağlık kurumunu girin")
                addDynamicField("Hizmet Türü", "Alınan hizmeti girin")
                addDynamicField("Fatura No", "Fatura numarasını girin")
                addDynamicField("Ödeme Tarihi", "Ödeme tarihini girin")
            }
            "Ulaşım" -> {
                addDynamicField("Araç Kredisi", "Araç kredi bilgilerini girin")
                addDynamicField("Akaryakıt", "Akaryakıt tutarını girin")
                addDynamicField("OGS/HGS", "Geçiş ücreti tutarını girin")
                addDynamicField("Servis", "Servis ücreti tutarını girin")
            }
            "Gıda/Market" -> {
                addDynamicField("Açıklama", "Alınan ürünleri girin")
                addDynamicField("Tarih", "Alışveriş tarihini girin")
            }
            "Alışveriş" -> {
                addDynamicField("Tür", "Giyim, Elektronik, Ev Eşyası")
                addDynamicField("Tarih", "Alışveriş tarihini girin")
            }
            "Tatil/Seyahat" -> {
                addDynamicField("Lokasyon", "Gidilen yeri girin")
                addDynamicField("Bilet", "Bilet tutarını girin")
                addDynamicField("Konaklama", "Konaklama tutarını girin")
                addDynamicField("Tarih", "Seyahat tarihini girin")
            }
            "Abonelikler" -> {
                addDynamicField("Tür", "Netflix, Spotify, Dergi vb.")
                addDynamicField("Periyot", "Abonelik periyodunu girin")
                addDynamicField("Son Ödeme", "Son ödeme tarihini girin")
            }
            "Diğer Giderler" -> {
                addDynamicField("Açıklama", "Detaylı açıklama girin")
                addDynamicField("Tür", "Manuel girilebilir")
                addDynamicField("Hatırlatma", "Hatırlatma tarihini girin")
            }
        }
    }

    private fun addDynamicField(label: String, hint: String) {
        val cardView = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            radius = 8f
            elevation = 2f
        }

        val textInputLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.hint = label
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val editText = TextInputEditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.hint = hint
        }

        textInputLayout.addView(editText)
        cardView.addView(textInputLayout)
        dynamicFieldsContainer.addView(cardView)
        
        dynamicFields[label] = editText
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                dateEditText.setText(dateFormat.format(Date(selectedDate)))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTransaction() {
        try {
            // Validate required fields
            if (titleEditText.text.isNullOrBlank()) {
                // Title is required
                return
            }
            
            val amount = amountEditText.text.toString().toDoubleOrNull()
            if (amount == null || amount <= 0) {
                // Invalid amount
                return
            }

            // Collect dynamic field data
            val metaData = mutableMapOf<String, Any>()
            dynamicFields.forEach { (key, editText) ->
                val value = editText.text.toString()
                if (value.isNotBlank()) {
                    metaData[key] = value
                }
            }

            // Add additional metadata
            metaData["subtype"] = selectedSubtype
            metaData["recurring"] = recurringSwitch.isChecked
            if (recurringSwitch.isChecked) {
                metaData["recurringInterval"] = recurringIntervalSpinner.text.toString()
            }
            metaData["reminder"] = reminderSwitch.isChecked

            val transaction = Transaction(
                type = selectedTransactionType,
                category = categorySpinner.text.toString(),
                subtype = selectedSubtype,
                title = titleEditText.text.toString(),
                amount = amount,
                currency = currencySpinner.text.toString(),
                date = selectedDate,
                description = descriptionEditText.text.toString(),
                isRecurring = recurringSwitch.isChecked,
                recurringInterval = if (recurringSwitch.isChecked) recurringIntervalSpinner.text.toString() else null,
                reminder = if (reminderSwitch.isChecked) System.currentTimeMillis() + (24 * 60 * 60 * 1000) else null,
                meta = if (metaData.isNotEmpty()) metaData else null
            )

            onTransactionSaved?.invoke(transaction)
            dismiss()
            
        } catch (e: Exception) {
            // Error saving transaction
        }
    }

    // Helper methods for display names
    private fun getIncomeSubtypeDisplayName(subtype: IncomeSubtype): String {
        return when (subtype) {
            IncomeSubtype.SALARY -> "Maaş"
            IncomeSubtype.FREELANCE -> "Serbest Gelir"
            IncomeSubtype.RENTAL_INCOME -> "Kira Geliri"
            IncomeSubtype.INVESTMENT_RETURN -> "Yatırım Getirisi"
            IncomeSubtype.RECEIVABLE -> "Alacak"
            IncomeSubtype.BONUS -> "Prim/Bonus"
            IncomeSubtype.PENSION -> "Emekli Maaşı"
            IncomeSubtype.INTEREST_DIVIDEND -> "Faiz/Temettü"
            IncomeSubtype.OTHER_INCOME -> "Diğer Gelirler"
        }
    }

    private fun getExpenseSubtypeDisplayName(subtype: ExpenseSubtype): String {
        return when (subtype) {
            ExpenseSubtype.RENT_EXPENSE -> "Kira Gideri"
            ExpenseSubtype.BANK_LOAN -> "Banka Kredisi"
            ExpenseSubtype.BILL_PAYMENT -> "Fatura Ödemeleri"
            ExpenseSubtype.INSURANCE -> "Sigorta"
            ExpenseSubtype.TAX_FEE -> "Vergi/Harç"
            ExpenseSubtype.EDUCATION -> "Eğitim"
            ExpenseSubtype.HEALTHCARE -> "Sağlık"
            ExpenseSubtype.TRANSPORTATION -> "Ulaşım"
            ExpenseSubtype.FOOD_GROCERY -> "Gıda/Market"
            ExpenseSubtype.SHOPPING -> "Alışveriş"
            ExpenseSubtype.TRAVEL -> "Tatil/Seyahat"
            ExpenseSubtype.SUBSCRIPTIONS -> "Abonelikler"
            ExpenseSubtype.OTHER_EXPENSE -> "Diğer Giderler"
        }
    }
}
