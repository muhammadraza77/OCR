package com.example.ocr.model

import androidx.lifecycle.LiveData
import androidx.room.*
import org.w3c.dom.Document


@androidx.room.Dao
interface TextDao {
    @Insert
    fun insert(model: TextModel):Long

    @Insert
    fun insert(model: DocumentModel):Long

    @Insert
    fun insertAll(model: List<TextModel>):List<Long>

    @Query("SELECT * FROM document_table where title=:documentName")
    fun getDocument(documentName:String):DocumentModel

    @Query("SELECT * FROM document_table")
    fun getAllDocuments(): List<DocumentModel>

    @Query("SELECT * FROM text_table")
    fun getAllText(): List<TextModel>

    @Update
    fun updateDocument(document: DocumentModel)

    @Transaction
    @Query("SELECT * FROM text_table where document_id=:documentId")
    fun getAllText(documentId:Long): List<TextModel>

    @Transaction
    @Query("DELETE from text_table where document_id is :documentId and text like :text")
    fun deleteText(documentId:Long,text:String)

    @Transaction
    @Query("DELETE from text_table where document_id is :documentId")
    fun deleteText(documentId:Long)

    @Transaction
    @Query("DELETE from document_table where title is :documentTitle")
    fun deleteDocument(documentTitle:String)
}