package com.example.ocr.model

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@androidx.room.Dao
interface TextDao {
    @Insert
    fun insert(model: TextModel):Long

    @Insert
    fun insert(model: DocumentModel):Long

    @Insert
    fun insertAll(model: List<TextModel>):List<Long>


    @Query("SELECT * FROM document_table")
    fun getAllDocuments(): List<DocumentModel>

    @Query("SELECT * FROM text_table")
    fun getAllText(): List<TextModel>

    @Transaction
    @Query("SELECT * FROM text_table where document_id=:documentId")
    fun getAllText(documentId:String): List<TextModel>

    @Transaction
    @Query("DELETE from text_table where document_id is :documentId and text like :text")
    fun deleteText(documentId:String,text:String)

    @Transaction
    @Query("DELETE from document_table where id is :documentId")
    fun deleteDocument(documentId:String)
}