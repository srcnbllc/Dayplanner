package com.example.dayplanner.features

/**
 * Modern not uygulamalarından popüler özellikler
 */
object ModernFeatures {

    /**
     * Hızlı etiketler
     */
    object QuickTags {
        val COMMON_TAGS = listOf(
            "önemli", "acil", "iş", "kişisel", "alışveriş", 
            "toplantı", "fikir", "proje", "seyahat", "sağlık"
        )
    }

    /**
     * Not arama önerileri
     */
    object SearchSuggestions {
        fun getSearchTips(): List<String> {
            return listOf(
                "Etiketlerle arama: #iş, #önemli",
                "Tarihle arama: bugün, dün, bu hafta",
                "Durumla arama: tamamlandı, bekliyor",
                "Öncelikle arama: yüksek, düşük",
                "Kategoriyle arama: iş, kişisel, alışveriş"
            )
        }
    }
}
