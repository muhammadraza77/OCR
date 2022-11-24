package com.example.ocr.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import com.example.ocr.constant.StorageKey
import dagger.Component

@SuppressLint("RestrictedApi")
class SharedPreferences(
    private val context: Context,
    private val pref: SharedPreferences = getApplicationContext(context)?.getSharedPreferences("MyPref", 0)
){
    fun writeKeyValue(key:StorageKey,value:Boolean){
        val editor = pref.edit()
        editor.putBoolean(key.name, value)
        editor.commit()
    }

    fun readValue(key: StorageKey):Boolean{
        return pref.getBoolean(key.name,false)
    }
}

