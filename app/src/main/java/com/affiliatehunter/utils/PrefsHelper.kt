package com.affiliatehunter.utils
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
private val Context.ds by preferencesDataStore("affiliate_prefs")
class PrefsHelper(private val c: Context){
    private val AFF = stringPreferencesKey("affiliate_id")
    private val CACHE = stringPreferencesKey("cache_json")
    private val BACKEND = stringPreferencesKey("backend_url")
    suspend fun getAffId(): String = c.ds.data.first()[AFF] ?: ""
    suspend fun setAffId(v: String){ c.ds.edit{ it[AFF]=v } }
    suspend fun getCache(): String = c.ds.data.first()[CACHE] ?: ""
    suspend fun setCache(v: String){ c.ds.edit{ it[CACHE]=v } }
    suspend fun getBackendUrl(): String = c.ds.data.first()[BACKEND] ?: ""
    suspend fun setBackendUrl(v: String){ c.ds.edit{ it[BACKEND]=v } }
}
