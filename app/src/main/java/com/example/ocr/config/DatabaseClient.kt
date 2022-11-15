package com.example.ocr.config

import android.content.Context
import androidx.room.Room


class DatabaseClient private constructor(private val mCtx: Context) {
    //our app database object
    val appDatabase: AppDatabase =  Room.databaseBuilder(mCtx, AppDatabase::class.java, "textDb").allowMainThreadQueries().build()

    companion object {
        private var mInstance: DatabaseClient? = null
        @Synchronized
        fun getInstance(mCtx: Context): DatabaseClient? {
            if (mInstance == null) {
                mInstance = DatabaseClient(mCtx)
            }
            return mInstance
        }
    }

}
