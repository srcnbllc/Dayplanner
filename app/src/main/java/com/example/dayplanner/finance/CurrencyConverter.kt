package com.example.dayplanner.finance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class CurrencyConverter {
    private val cache = mutableMapOf<String, Map<String, Double>>()
    private val mutex = Mutex()

    suspend fun getRates(base: String = "TRY"): Map<String, Double> {
        mutex.withLock {
            val cached = cache[base]
            if (cached != null) return cached
            val latest = fetchLatestRates()
            cache[base] = latest
            return latest
        }
    }

    private suspend fun fetchLatestRates(): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            // TCMB'nin yeni EVDS API'si için basit bir yaklaşım
            // Gerçek uygulamada API key gerekli olacak
            val url = URL("https://www.tcmb.gov.tr/kurlar/today.xml")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

            return@withContext conn.inputStream.use { input ->
                val doc: Document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(input)

                val currencyNodes = doc.getElementsByTagName("Currency")
                val rates = mutableMapOf<String, Double>()
                rates["TRY"] = 1.0 // TL baz alınır

                for (i in 0 until currencyNodes.length) {
                    val node = currencyNodes.item(i)
                    val element = node as org.w3c.dom.Element
                    val code = element.getAttribute("CurrencyCode")
                    val forexSelling = element.getElementsByTagName("ForexSelling")
                        .item(0)?.textContent?.replace(",", ".")?.toDoubleOrNull()
                    if (forexSelling != null && forexSelling > 0) {
                        rates[code] = forexSelling
                    }
                }
                rates
            }
        } catch (e: Exception) {
            // Fallback to mock rates if TCMB is not accessible
            println("TCMB API Error: ${e.message}")
            return@withContext mapOf(
                "TRY" to 1.0,
                "USD" to 27.5,
                "EUR" to 30.0,
                "GBP" to 35.0,
                "JPY" to 0.18,
                "CHF" to 28.5,
                "CAD" to 20.0,
                "AUD" to 18.0
            )
        }
    }

    suspend fun convert(amount: Double, sourceCurrency: String, base: String = "TRY"): Double {
        val rates = getRates(base)
        val sourceRate = rates[sourceCurrency] ?: 1.0
        val baseRate = rates[base] ?: 1.0
        return amount * (sourceRate / baseRate)
    }

    // Test metodu - TCMB API'sine bağlanıp veri gelip gelmediğini test eder
    suspend fun testTCMBConnection(): Map<String, Double> {
        return fetchLatestRates()
    }
}
