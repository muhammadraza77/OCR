package com.example.ocr.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_table")
data class TextModel(
    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name = "text")
    var text:String
)