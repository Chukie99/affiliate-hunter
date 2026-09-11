package com.affiliatehunter.ui
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.affiliatehunter.utils.PrefsHelper
import com.affiliatehunter.data.remote.ShopeeRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
@HiltViewModel
class SettingsViewModel @Inject constructor(@ApplicationContext private val ctx: Context): ViewModel(){
    private val prefs = PrefsHelper(ctx)
    private val _affId = MutableStateFlow("")
    val affId = _affId.asStateFlow()
    private val _backendUrl = MutableStateFlow("")
    val backendUrl = _backendUrl.asStateFlow()
    init{
        viewModelScope.launch{
            _affId.value=prefs.getAffId()
            _backendUrl.value=prefs.getBackendUrl()
            if(_backendUrl.value.isNotBlank()) ShopeeRemote.backendBaseUrl = _backendUrl.value
        }
    }
    fun saveAffId(v:String){ viewModelScope.launch{ prefs.setAffId(v); _affId.value=v } }
    fun saveBackendUrl(v:String){ viewModelScope.launch{ prefs.setBackendUrl(v.trim()); _backendUrl.value=v.trim(); ShopeeRemote.backendBaseUrl = v.trim().ifBlank{ null } } }
}
