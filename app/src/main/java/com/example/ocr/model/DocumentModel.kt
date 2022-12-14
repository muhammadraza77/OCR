package com.example.ocr.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "document_table")
data class DocumentModel(
    @PrimaryKey(autoGenerate = true)
    var id:Long,

    @ColumnInfo(name = "text")
    var title:String,

    @ColumnInfo(name = "audio_id")
    var audioId:String

)