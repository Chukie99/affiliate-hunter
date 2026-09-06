package com.affiliatehunter.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.affiliatehunter.data.database.FavoriteDao
import com.affiliatehunter.data.model.FavoriteEntity
import com.affiliatehunter.data.model.Product
import com.affiliatehunter.data.remote.ShopeeRemote
import com.affiliatehunter.utils.AffiliateLink
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val hideFlagged:Boolean=false, val opOnly:Boolean=false
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val favDao: FavoriteDao): ViewModel(){
    private val _s = MutableStateFlow(UiState())
    val s = _s.asStateFlow()

    init{ viewModelScope.launch{ val all=favDao.getAll(); _s.value=_s.value.copy(favorites=all.map{it.id}.toSet()) } }

    fun setQuery(v:String){ _s.value=_s.value.copy(query=v) }
    fun setMinSold(v:Int){ _s.value=_s.value.copy(minSold=v); applyFilters() }
    fun setMinRating(v:Double){ _s.value=_s.value.copy(minRating=v); applyFilters() }
    fun setPrice(min:Long,max:Long){ _s.value=_s.value.copy(minPrice=min,maxPrice=max); applyFilters() }
    fun setAffId(v:String){ _s.value=_s.value.copy(affId=v) }
    fun setHideFlagged(v:Boolean){ _s.value=_s.value.copy(hideFlagged=v); applyFilters() }
    fun setOpOnly(v:Boolean){ _s.value=_s.value.copy(opOnly=v); applyFilters() }

    fun search(){
        viewModelScope.launch{
            _s.value=_s.value.copy(loading=true, error=null)
            try{
                val raw = ShopeeRemote.search(_s.value.query, 30)
                if(raw.isEmpty()){ _s.value=_s.value.copy(loading=false, error="Gagal fetch / diblokir Shopee. Coba ganti keyword atau ulangi."); return@launch }
                val cur=_s.value
                var list = raw.filter{ it.sold>=cur.minSold && it.rating>=cur.minRating && it.price in cur.minPrice..cur.maxPrice }
                list = list.sortedByDescending{ it.score }
                _s.value=_s.value.copy(products=list, loading=false); applyFilters()
            }catch(e:Exception){
                _s.value=_s.value.copy(loading=false, error=e.message?:"Gagal fetch")
            }
        }
    }
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
