package com.example.dayplanner.finance

enum class TransactionType {
    INCOME,    // Gelir
    EXPENSE    // Gider
}

enum class PaymentType {
    CASH,           // Nakit
    CREDIT_CARD,    // Kredi Kartı
    DEBIT_CARD,     // Banka Kartı
    BANK_TRANSFER,  // Havale/EFT
    CHECK,          // Çek
    DIGITAL_WALLET, // Dijital Cüzdan
    CRYPTOCURRENCY, // Kripto Para
    OTHER          // Diğer
}

enum class TransactionStatus {
    ACTIVE,     // Aktif
    PENDING,    // Beklemede
    CANCELLED,  // İptal Edildi
    COMPLETED   // Tamamlandı
}
