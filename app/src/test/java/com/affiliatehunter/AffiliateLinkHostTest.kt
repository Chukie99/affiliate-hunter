package com.affiliatehunter
import com.affiliatehunter.utils.AffiliateLink
import org.junit.Test
import org.junit.Assert.*
class AffiliateLinkHostTest{
    @Test fun append_query(){
        val a = AffiliateLink.toAffiliate("https://shopee.co.id/a?x=1", "aff_1")
        assertTrue(a.contains("&aff="))
    }
    @Test fun append_noQuery(){
        val a = AffiliateLink.toAffiliate("https://shopee.co.id/a", "aff_1")
        assertTrue(a.contains("?aff="))
    }
}
