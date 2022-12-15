package com.example.ocr.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey



@Entity(
    tableName = "document_table",
    indices = [Index(value = ["title"], unique = true)]
)
data class DocumentModel(
    @PrimaryKey(autoGenerate = true)
    var id:Long,

    @ColumnInfo(name = "title")
    var title:String,

    @ColumnInfo(name = "audio_id")
    var audioId:String

)