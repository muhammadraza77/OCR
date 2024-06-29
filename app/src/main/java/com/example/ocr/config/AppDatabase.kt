package com.example.ocr.config

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ocr.model.DocumentModel
import com.example.ocr.model.TextDao
import com.example.ocr.model.TextModel


@Database(entities = [TextModel::class,DocumentModel::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textDao(): TextDao?
}
