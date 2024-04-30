package com.yogesh.videoplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Session @Inject constructor(@ApplicationContext context: Context) {
    private var sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun saveData(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun saveBool(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getData(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun getBool(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveObject(key: String, classOfT: Class<*>): Any {
        val json = getData(key)
        return Gson().fromJson(json, classOfT)
    }

    fun getObject(key: String, obj: Any) {
        val gson = Gson()
        saveData(key, gson.toJson(obj))
    }

    fun containsKeyOrNot(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun removeKey(key: String) {
        if (sharedPreferences.contains(key)) {
            editor.remove(key)
            editor.apply()
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

