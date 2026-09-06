package com.affiliatehunter
import com.affiliatehunter.utils.Scoring
import com.affiliatehunter.utils.AffiliateLink
import org.junit.Test
import org.junit.Assert.*

class ScoringHostTest{
    @Test fun score_basic(){
        val s = Scoring.score(sold=1000, rating=4.6, price=100000)
        assertTrue(s > 0)
    }
    @Test fun score_hargaSweetSpot_lebihTinggi(){
        val sweet = Scoring.score(2000,4.7,100000)
        val mahal = Scoring.score(2000,4.7,600000)
        assertTrue(sweet > mahal)
    }
    @Test fun score_log10_scaling(){
        val low = Scoring.score(100,4.5,100000)
        val high = Scoring.score(10000,4.5,100000)
        assertTrue(high > low)
    }
    @Test fun commission_8percent(){
        assertEquals(8000L, Scoring.commission(100000,0.08))
    }
    @Test fun trustFlag_tokoBaru_flagged(){
        val (flag, _) = Scoring.trustFlag(shopAgeDays=5, reviewCount=100, rating=4.7)
        assertTrue(flag)
    }
    @Test fun trustFlag_tokoLama_ok(){
        val (flag, _) = Scoring.trustFlag(60, 500, 4.7)
        assertFalse(flag)
    }
    @Test fun affiliateLink_withAff(){
        val out = AffiliateLink.toAffiliate("https://shopee.co.id/product-i.1.2", "aff_123")
        assertTrue(out.contains("aff_123"))
    }
    @Test fun affiliateLink_tanpaAff_tetapOriginal(){
        val ori="https://shopee.co.id/x"
        assertEquals(ori, AffiliateLink.toAffiliate(ori, ""))
    }
}
