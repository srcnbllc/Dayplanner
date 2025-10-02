package com.example.dayplanner.ui.finance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentCurrencyConverterBinding
import com.example.dayplanner.finance.CurrencyConverter
import com.example.dayplanner.utils.CustomToast
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CurrencyConverterFragment : Fragment() {

    private var _binding: FragmentCurrencyConverterBinding? = null
    private val binding get() = _binding!!

    private lateinit var currencyConverter: CurrencyConverter
    private var currentRates: Map<String, Double> = emptyMap()
    private var isUpdating = false

    // Dünya para birimleri listesi
    private val currencies = listOf(
        "TRY", "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK",
        "PLN", "CZK", "HUF", "RUB", "CNY", "INR", "KRW", "SGD", "HKD", "MXN", "BRL", "ZAR",
        "AED", "SAR", "QAR", "KWD", "BHD", "OMR", "JOD", "LBP", "EGP", "MAD", "TND", "DZD",
        "LYD", "SDG", "ETB", "KES", "UGX", "TZS", "RWF", "BIF", "DJF", "SOS", "ERN", "ETB",
        "GMD", "GNF", "LRD", "SLL", "CDF", "AOA", "BWP", "LSL", "SZL", "ZMW", "MWK", "MZN",
        "SCR", "MUR", "KMF", "MGA", "BIF", "RWF", "TZS", "UGX", "KES", "ETB", "SDG", "LYD",
        "DZD", "TND", "MAD", "EGP", "LBP", "JOD", "OMR", "BHD", "KWD", "QAR", "SAR", "AED"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyConverterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        currencyConverter = CurrencyConverter()
        setupUI()
        loadExchangeRates()
    }

    private fun setupUI() {
        // Setup currency spinners
        setupCurrencySpinners()
        
        // Setup amount input
        setupAmountInput()
        
        // Setup convert button
        setupConvertButton()
        
        // Setup TCMB query button
        setupTCMBQueryButton()
        
        // Setup swap button
        setupSwapButton()
        
        // Setup refresh button
        setupRefreshButton()
        
        // Setup back button
        setupBackButton()
    }

    private fun setupCurrencySpinners() {
        val currencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        
        binding.fromCurrencySpinner.setAdapter(currencyAdapter)
        binding.toCurrencySpinner.setAdapter(currencyAdapter)
        
        // Set default values
        binding.fromCurrencySpinner.setText("TRY", false)
        binding.toCurrencySpinner.setText("USD", false)
        
        // Listen for currency changes
        binding.fromCurrencySpinner.setOnItemClickListener { _, _, _, _ ->
            if (!isUpdating) {
                performConversion()
            }
        }
        
        binding.toCurrencySpinner.setOnItemClickListener { _, _, _, _ ->
            if (!isUpdating) {
                performConversion()
            }
        }
    }

    private fun setupAmountInput() {
        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating && s.toString().isNotEmpty()) {
                    performConversion()
                }
            }
        })
    }

    private fun setupConvertButton() {
        binding.convertButton.setOnClickListener {
            performConversion()
        }
    }

    private fun setupSwapButton() {
        binding.swapButton.setOnClickListener {
            swapCurrencies()
        }
    }

    private fun setupTCMBQueryButton() {
        binding.tcmbQueryButton.setOnClickListener {
            loadExchangeRates()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            loadExchangeRates()
        }
    }

    private fun swapCurrencies() {
        isUpdating = true
        
        val fromCurrency = binding.fromCurrencySpinner.text.toString()
        val toCurrency = binding.toCurrencySpinner.text.toString()
        
        binding.fromCurrencySpinner.setText(toCurrency, false)
        binding.toCurrencySpinner.setText(fromCurrency, false)
        
        isUpdating = false
        
        // Perform conversion after swap
        performConversion()
    }

    private fun loadExchangeRates() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.refreshButton.isEnabled = false
                
                // Exchange rates loading
                
                currentRates = currencyConverter.getRates("TRY")
                
                binding.progressBar.visibility = View.GONE
                binding.refreshButton.isEnabled = true
                
                // Exchange rates updated successfully
                
                // Perform conversion with new rates
                performConversion()
                
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.refreshButton.isEnabled = true
                // Error loading exchange rates
            }
        }
    }

    private fun performConversion() {
        val amountText = binding.amountInput.text.toString()
        if (amountText.isEmpty()) {
            binding.resultText.text = "0.00"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.resultText.text = "Geçersiz tutar"
            return
        }

        val fromCurrency = binding.fromCurrencySpinner.text.toString()
        val toCurrency = binding.toCurrencySpinner.text.toString()

        if (fromCurrency.isEmpty() || toCurrency.isEmpty()) {
            binding.resultText.text = "Para birimi seçin"
            return
        }

        if (fromCurrency == toCurrency) {
            binding.resultText.text = formatAmount(amount, toCurrency)
            binding.rateText.text = "1 $fromCurrency = 1.00 $toCurrency"
            return
        }

        // Use cached rates for conversion
        if (currentRates.isNotEmpty()) {
            try {
                val fromRate = currentRates[fromCurrency] ?: 1.0
                val toRate = currentRates[toCurrency] ?: 1.0
                
                // Convert to TRY first, then to target currency
                val amountInTRY = amount * fromRate
                val convertedAmount = amountInTRY / toRate
                
                binding.resultText.text = formatAmount(convertedAmount, toCurrency)
                binding.rateText.text = "1 $fromCurrency = ${formatAmount(toRate / fromRate, toCurrency)}"
                
            } catch (e: Exception) {
                binding.resultText.text = "Çevirme hatası"
                binding.rateText.text = ""
            }
        } else {
            binding.resultText.text = "Önce TCMB'den sorgula"
            binding.rateText.text = ""
        }
    }

    private suspend fun getConversionRate(fromCurrency: String, toCurrency: String): Double {
        return try {
            currencyConverter.convert(1.0, fromCurrency, toCurrency)
        } catch (e: Exception) {
            0.0
        }
    }

    private fun formatAmount(amount: Double, currency: String): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        
        return "${formatter.format(amount)} $currency"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
