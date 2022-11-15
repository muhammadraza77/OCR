package com.example.ocr.model

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@androidx.room.Dao
interface TextDao {
    @Insert
    fun insert(model: TextModel)

    @Query("DELETE from text_table where text like :input")
    fun delete(input:String)

    @Query("SELECT * FROM text_table")
    fun getAll(): List<TextModel>
}