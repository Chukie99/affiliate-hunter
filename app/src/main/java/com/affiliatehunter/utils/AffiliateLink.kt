package com.affiliatehunter.utils
object AffiliateLink {
    fun toAffiliate(original: String, affId: String): String {
        if(affId.isBlank()) return original
        val sep = if(original.contains("?")) "&" else "?"
        return "$original${sep}aff=$affId"
    }
}
