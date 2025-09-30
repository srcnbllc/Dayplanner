package com.example.dayplanner.ui.finance

import android.app.DatePickerDialog
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionDialog : DialogFragment() {

    private lateinit var transactionTypeSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var subtypeSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var currencySpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var categorySpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var dateEditText: TextInputEditText
    private lateinit var recurringSwitch: Switch
    private lateinit var recurringIntervalLayout: TextInputLayout
    private lateinit var recurringIntervalSpinner: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var reminderSwitch: Switch
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var dynamicFieldsContainer: LinearLayout
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private var onTransactionSaved: ((Transaction) -> Unit)? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        fun newInstance(onTransactionSaved: (Transaction) -> Unit): AddTransactionDialog {
            return AddTransactionDialog().apply {
                this.onTransactionSaved = onTransactionSaved
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_transaction, container, false)
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
        val currencies = listOf("TRY", "USD", "EUR", "GBP")
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
            val selectedType = if (position == 0) TransactionType.INCOME else TransactionType.EXPENSE
            updateSubtypes(selectedType)
            updateCategories(selectedType)
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
            TransactionType.INCOME -> IncomeSubtype.values().map { it.name }
            TransactionType.EXPENSE -> ExpenseSubtype.values().map { it.name }
        }
        val subtypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subtypes)
        subtypeSpinner.setAdapter(subtypeAdapter)
    }

    private fun updateCategories(transactionType: TransactionType) {
        val categories = when (transactionType) {
            TransactionType.INCOME -> listOf("Maaş", "Serbest Gelir", "Kira Geliri", "Yatırım", "Diğer")
            TransactionType.EXPENSE -> listOf("Kira", "Fatura", "Yemek", "Ulaşım", "Sağlık", "Eğitim", "Diğer")
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categorySpinner.setAdapter(categoryAdapter)
    }

    private fun setupDynamicFields() {
        dynamicFieldsContainer.removeAllViews()
        
        val transactionType = if (transactionTypeSpinner.text.toString() == "Gelir") TransactionType.INCOME else TransactionType.EXPENSE
        val subtype = subtypeSpinner.text.toString()

        when (transactionType) {
            TransactionType.INCOME -> setupIncomeFields(subtype)
            TransactionType.EXPENSE -> setupExpenseFields(subtype)
        }
    }

    private fun setupIncomeFields(subtype: String) {
        when (subtype) {
            "SALARY" -> {
                addTextField("İşveren", "employer")
                addTextField("Net/Gross Tutar", "netGross")
                addTextField("Ödeme Günü", "paymentDay")
                addTextField("Banka Hesabı", "bankAccount")
                addTextField("SSK/SGK Kesintisi", "deduction")
            }
            "FREELANCE" -> {
                addTextField("Fatura No", "invoiceNumber")
                addTextField("Müşteri", "customer")
                addTextField("KDV", "vat")
                addTextField("Ödeme Şekli", "paymentMethod")
            }
            "RENTAL_INCOME" -> {
                addTextField("Sözleşme Tarihi", "contractDate")
                addTextField("Süre", "duration")
                addTextField("Aylık Tutar", "monthlyAmount")
                addTextField("Ödeme Günü", "paymentDay")
                addTextField("Banka", "bank")
            }
            "INVESTMENT_RETURN" -> {
                addTextField("Tür (Hisse, Tahvil, Mevduat, Kripto)", "investmentType")
                addTextField("Anapara", "principal")
                addTextField("Faiz/Kâr Oranı", "interestRate")
                addTextField("Periyot", "period")
                addTextField("Vade", "maturity")
            }
            "RECEIVABLE" -> {
                addTextField("Kişi/Kurum", "personCompany")
                addTextField("Tahsil Tarihi", "collectionDate")
                addTextField("Hatırlatma", "reminder")
            }
            "BONUS" -> {
                addTextField("Kaynak", "source")
                addTextField("Tarih", "bonusDate")
            }
            "PENSION" -> {
                addTextField("Kurum", "institution")
                addTextField("Tarih", "pensionDate")
                addTextField("Banka", "bank")
            }
            "INTEREST_DIVIDEND" -> {
                addTextField("Tür", "type")
                addTextField("Oran", "rate")
                addTextField("Vade", "maturity")
            }
            "OTHER_INCOME" -> {
                addTextField("Kaynak", "source")
                addTextField("Açıklama", "description")
            }
        }
    }

    private fun setupExpenseFields(subtype: String) {
        when (subtype) {
            "RENT_EXPENSE" -> {
                addTextField("Sözleşme", "contract")
                addTextField("Süre", "duration")
                addTextField("Ödeme Planı", "paymentPlan")
                addTextField("Vade", "dueDate")
                addTextField("Kapora", "deposit")
                addTextField("Banka", "bank")
            }
            "BANK_LOAN" -> {
                addTextField("Kredi Tutarı", "loanAmount")
                addTextField("Faiz Oranı", "interestRate")
                addTextField("Taksit Sayısı", "installmentCount")
                addTextField("Aylık Tutar", "monthlyAmount")
                addTextField("Son Ödeme Tarihi", "lastPaymentDate")
            }
            "BILL_PAYMENT" -> {
                addTextField("Tür (Elektrik, Su, Doğalgaz, İnternet, Telefon, TV)", "billType")
                addTextField("Abone No", "subscriberNumber")
                addTextField("Dönem", "period")
                addTextField("Son Tarih", "dueDate")
                addTextField("Otomatik Ödeme", "autoPayment")
            }
            "INSURANCE" -> {
                addTextField("Tür (Sağlık, Araç, Konut, Hayat)", "insuranceType")
                addTextField("Poliçe No", "policyNumber")
                addTextField("Prim Tutarı", "premiumAmount")
                addTextField("Periyot", "period")
                addTextField("Şirket", "company")
                addTextField("Son Tarih", "dueDate")
            }
            "TAX_FEE" -> {
                addTextField("Tür (Gelir, KDV, MTV, Tapu)", "taxType")
                addTextField("Dönem", "period")
                addTextField("Ödeme Tarihi", "paymentDate")
            }
            "EDUCATION" -> {
                addTextField("Kurum", "institution")
                addTextField("Eğitim Türü (Okul, Kurs)", "educationType")
                addTextField("Fatura No", "invoiceNumber")
                addTextField("Ödeme Tarihi", "paymentDate")
            }
            "HEALTHCARE" -> {
                addTextField("Kurum/Hastane", "institution")
                addTextField("Hizmet Türü", "serviceType")
                addTextField("Fatura No", "invoiceNumber")
                addTextField("Ödeme Tarihi", "paymentDate")
            }
            "TRANSPORTATION" -> {
                addTextField("Araç Kredisi", "vehicleLoan")
                addTextField("Akaryakıt", "fuel")
                addTextField("OGS/HGS", "toll")
                addTextField("Servis", "service")
            }
            "FOOD_GROCERY" -> {
                addTextField("Açıklama", "description")
            }
            "SHOPPING" -> {
                addTextField("Tür (Giyim, Elektronik, Ev Eşyası)", "shoppingType")
            }
            "TRAVEL" -> {
                addTextField("Lokasyon", "location")
                addTextField("Bilet", "ticket")
                addTextField("Konaklama", "accommodation")
            }
            "SUBSCRIPTIONS" -> {
                addTextField("Netflix, Spotify, Dergi vb.", "subscriptionType")
                addTextField("Periyot", "period")
                addTextField("Son Ödeme", "lastPayment")
            }
            "OTHER_EXPENSE" -> {
                addTextField("Açıklama", "description")
                addTextField("Tür (manuel girilebilir)", "manualType")
            }
        }
    }

    private fun addTextField(hint: String, fieldName: String) {
        val textInputLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            this.hint = hint
        }

        val editText = TextInputEditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tag = fieldName
        }

        textInputLayout.addView(editText)
        dynamicFieldsContainer.addView(textInputLayout)
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
        if (validateInput()) {
            val transaction = createTransaction()
            onTransactionSaved?.invoke(transaction)
            dismiss()
        }
    }

    private fun validateInput(): Boolean {
        if (titleEditText.text.toString().trim().isEmpty()) {
            CustomToast.show(requireContext(), "Başlık boş olamaz")
            return false
        }

        if (amountEditText.text.toString().trim().isEmpty()) {
            CustomToast.show(requireContext(), "Tutar boş olamaz")
            return false
        }

        try {
            val amount = amountEditText.text.toString().toDouble()
            if (amount <= 0) {
                CustomToast.show(requireContext(), "Tutar pozitif olmalıdır")
                return false
            }
        } catch (e: NumberFormatException) {
            CustomToast.show(requireContext(), "Geçerli bir tutar giriniz")
            return false
        }

        return true
    }

    private fun createTransaction(): Transaction {
        val transactionType = if (transactionTypeSpinner.text.toString() == "Gelir") TransactionType.INCOME else TransactionType.EXPENSE
        val subtype = subtypeSpinner.text.toString()
        val amount = amountEditText.text.toString().toDouble()
        val currency = currencySpinner.text.toString()
        val category = categorySpinner.text.toString()
        val isRecurring = recurringSwitch.isChecked
        val recurringInterval = if (isRecurring) recurringIntervalSpinner.text.toString() else null
        val reminder = reminderSwitch.isChecked

        // Collect dynamic fields
        val meta = mutableMapOf<String, Any>()
        for (i in 0 until dynamicFieldsContainer.childCount) {
            val textInputLayout = dynamicFieldsContainer.getChildAt(i) as TextInputLayout
            val editText = textInputLayout.getChildAt(0) as TextInputEditText
            val fieldName = editText.tag as String
            val fieldValue = editText.text.toString()
            if (fieldValue.isNotEmpty()) {
                meta[fieldName] = fieldValue
            }
        }

        return Transaction(
            type = transactionType,
            subtype = subtype,
            title = titleEditText.text.toString(),
            amount = amount,
            currency = currency,
            category = category,
            description = descriptionEditText.text.toString(),
            date = selectedDate,
            isRecurring = isRecurring,
            recurringInterval = recurringInterval,
            reminder = reminder,
            meta = if (meta.isNotEmpty()) com.google.gson.Gson().toJson(meta) else null
        )
    }
}
