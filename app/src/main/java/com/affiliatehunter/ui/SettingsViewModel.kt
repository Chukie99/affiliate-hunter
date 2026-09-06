package com.affiliatehunter.ui
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.affiliatehunter.utils.PrefsHelper
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
    init{ viewModelScope.launch{ _affId.value=prefs.getAffId() } }
    fun saveAffId(v:String){ viewModelScope.launch{ prefs.setAffId(v); _affId.value=v } }
}
