package com.example.ocr.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

class DocumentWithTextModel(
    @Embedded val document: DocumentModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "document_id"
    )
    val textList: List<TextModel>
)
