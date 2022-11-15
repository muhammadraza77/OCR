package com.example.ocr.config

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ocr.model.TextDao
import com.example.ocr.model.TextModel


@Database(entities = [TextModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textDao(): TextDao?
}
