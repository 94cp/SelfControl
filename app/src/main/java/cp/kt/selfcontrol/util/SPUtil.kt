package cp.kt.selfcontrol.util

import android.content.Context
import android.content.SharedPreferences
import cp.kt.selfcontrol.MainApplication

object SPUtil {
    var sp: SharedPreferences = MainApplication.context.getSharedPreferences(
        MainApplication.context.packageName,
        Context.MODE_PRIVATE
    )

    fun contains(key: String): Boolean {
        return sp.contains(key)
    }

    fun save(key: String, value: String?) {
        sp.edit().putString(key, value).apply()
    }

    fun get(key: String, def: String): String? {
        return sp.getString(key, def)
    }

    fun save(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun get(key: String, def: Boolean): Boolean {
        return sp.getBoolean(key, def)
    }

    fun save(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun get(key: String, def: Int): Int {
        return sp.getInt(key, def)
    }

    fun saveSet(key: String, set: Set<String>?) {
        sp.edit().putStringSet(key, set).apply()
    }

    fun getSet(key: String, def: Set<String> = HashSet<String>()): Set<String>? {
        return sp.getStringSet(key, def)
    }
}