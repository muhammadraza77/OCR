package com.example.ocr.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.w3c.dom.Document

@Entity(
    tableName = "text_table"
)
data class TextModel(
    @PrimaryKey(autoGenerate = true)
    var id:Long,

    @ColumnInfo(name = "document_id")
    var document_id:Long,

    @ColumnInfo(name = "text")
    var text:String
)

