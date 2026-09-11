package com.affiliatehunter.ui
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.affiliatehunter.data.database.FavoriteDao
import com.affiliatehunter.data.model.FavoriteEntity
import com.affiliatehunter.data.model.Product
import com.affiliatehunter.data.remote.ShopeeRemote
import com.affiliatehunter.utils.AffiliateLink
import com.affiliatehunter.utils.PrefsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val query:String="parfum", val minSold:Int=1000, val minRating:Double=4.6,
    val minPrice:Long=50000, val maxPrice:Long=300000, val affId:String="",
    val loading:Boolean=false, val error:String? = null,
    val products:List<Product> = emptyList(), val filtered:List<Product> = emptyList(),
    val favorites:Set<String> = emptySet(),
    val hideFlagged:Boolean=false, val opOnly:Boolean=false,
    val showOnboarding:Boolean=true,
    val page:Int=1
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val favDao: FavoriteDao,
    @ApplicationContext private val ctx: Context
): ViewModel(){
    private val prefs = PrefsHelper(ctx)
    private val _s = MutableStateFlow(UiState())
    val s = _s.asStateFlow()

    init{
        viewModelScope.launch{
            val all=favDao.getAll()
            val aff = prefs.getAffId()
            val backend = prefs.getBackendUrl()
            if(backend.isNotBlank()) ShopeeRemote.backendBaseUrl = backend
            val onboardingDone = prefs.getCache().contains("onboarding_done")
            _s.value=_s.value.copy(favorites=all.map{it.id}.toSet(), affId=aff, showOnboarding=!onboardingDone)
        }
    }

    fun dismissOnboarding(){ viewModelScope.launch{ prefs.setCache(prefs.getCache()+"|onboarding_done"); _s.value=_s.value.copy(showOnboarding=false) } }
    fun setQuery(v:String){ _s.value=_s.value.copy(query=v) }
    fun setMinSold(v:Int){ _s.value=_s.value.copy(minSold=v); applyFilters() }
    fun setMinRating(v:Double){ _s.value=_s.value.copy(minRating=v); applyFilters() }
    fun setPrice(min:Long,max:Long){ _s.value=_s.value.copy(minPrice=min,maxPrice=max); applyFilters() }
    fun setAffId(v:String){ _s.value=_s.value.copy(affId=v) }
    fun setHideFlagged(v:Boolean){ _s.value=_s.value.copy(hideFlagged=v); applyFilters() }
    fun setOpOnly(v:Boolean){ _s.value=_s.value.copy(opOnly=v); applyFilters() }
    fun refreshAffId(){ viewModelScope.launch{ _s.value=_s.value.copy(affId=prefs.getAffId()) } }

    fun search(loadMore:Boolean=false){
        viewModelScope.launch{
            _s.value=_s.value.copy(loading=true, error=null)
            try{
                // refresh affId/backend before search
                val aff = prefs.getAffId()
                val backend = prefs.getBackendUrl()
                if(backend.isNotBlank()) ShopeeRemote.backendBaseUrl = backend
                if(aff.isNotBlank() && aff != _s.value.affId) _s.value=_s.value.copy(affId=aff)
                val raw = ShopeeRemote.search(_s.value.query, 30, _s.value.affId)
                if(raw.isEmpty()){ _s.value=_s.value.copy(loading=false, error="Gagal fetch / diblokir Shopee. Coba ganti keyword atau set Backend API di Pengaturan."); return@launch }
                val cur=_s.value
                var list = raw.filter{ it.sold>=cur.minSold && it.rating>=cur.minRating && it.price in cur.minPrice..cur.maxPrice }
                list = list.sortedByDescending{ it.score }
                // if loadMore, append (dedup by id)
                val merged = if(loadMore) (cur.products + list).distinctBy{ it.id }.sortedByDescending{ it.score } else list
                _s.value=_s.value.copy(products=merged, loading=false); applyFilters()
            }catch(e:Exception){
                _s.value=_s.value.copy(loading=false, error=e.message?:"Gagal fetch")
            }
        }
    }
    fun loadMore(){ search(loadMore=true) }

    private fun applyFilters(){
        var list = _s.value.products
        if(_s.value.opOnly) list = list.filter{ it.isOP }
        if(_s.value.hideFlagged) list = list.filter{ !it.isFlagged }
        _s.value=_s.value.copy(filtered=list)
    }
    fun toggleFav(p: Product){
        viewModelScope.launch{
            val isFav = _s.value.favorites.contains(p.id)
            if(isFav){ favDao.delete(p.id) } else {
                favDao.insert(FavoriteEntity(id=p.id, name=p.name, price=p.price, sold=p.sold, rating=p.rating, link=p.link, affiliateLink=AffiliateLink.toAffiliate(p.link,_s.value.affId), category=p.category))
            }
            val all=favDao.getAll(); _s.value=_s.value.copy(favorites=all.map{it.id}.toSet())
        }
    }
}
