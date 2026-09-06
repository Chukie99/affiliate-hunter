package com.affiliatehunter.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.affiliatehunter.data.database.FavoriteDao
import com.affiliatehunter.data.model.FavoriteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
@HiltViewModel
class FavoritesViewModel @Inject constructor(private val dao: FavoriteDao): ViewModel(){
    private val _items = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val items = _items.asStateFlow()
    init{ refresh() }
    fun refresh(){ viewModelScope.launch{ _items.value=dao.getAll() } }
    fun remove(id:String){ viewModelScope.launch{ dao.delete(id); _items.value=dao.getAll() } }
    fun clear(){ viewModelScope.launch{ dao.clear(); _items.value=emptyList() } }
}
