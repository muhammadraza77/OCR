package com.example.ocr.activity

import android.os.Bundle
import android.view.KeyEvent
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.model.TextModel


class ConfigureActivity : AppCompatActivity() {

    private var listView: ListView? = null
    private var editText: EditText? = null
    private var saveButton:Button?=null

    private var db : AppDatabase? = null

    private var itemsAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        bindUIComponents()

        db = DatabaseClient.getInstance(applicationContext)!!.appDatabase
        val currentDataFromDb = fetchAllWords()

        setupListview(currentDataFromDb)

        editText!!.setImeActionLabel("Save", KeyEvent.KEYCODE_ENTER);
        saveButton!!.setOnClickListener{
            val text = editText!!.text.toString()
            insertWord(text)
            currentDataFromDb!!.add(text)
            itemsAdapter!!.notifyDataSetChanged()
            editText!!.setText("")
        }

    }

    private fun insertWord(text: String) {
        db!!.textDao()?.insert(TextModel(0, text))
    }

    private fun fetchAllWords() = db!!.textDao()?.getAll()?.map { it.text }?.toMutableList()

    private fun setupListview(currentDataFromDb: MutableList<String>?) {
        itemsAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_1,
            currentDataFromDb!!
        )
        listView!!.adapter = itemsAdapter

        listView!!.isLongClickable = true
        listView!!.onItemLongClickListener =
            OnItemLongClickListener { _, _, pos, id -> // TODO Auto-generated method stub
                Toast.makeText(
                    applicationContext,
                    currentDataFromDb[pos] + " Removed Database",
                    Toast.LENGTH_SHORT
                ).show()
                deleteWord(currentDataFromDb, pos)
                currentDataFromDb.removeAt(pos)
                itemsAdapter!!.notifyDataSetChanged()
                true
            }
    }

    private fun deleteWord(
        currentDataFromDb: MutableList<String>,
        pos: Int
    ) {
        db!!.textDao()?.delete(currentDataFromDb[pos])
    }

    private fun bindUIComponents() {
        listView = findViewById(R.id.textListView)
        editText = findViewById(R.id.textInput)
        saveButton = findViewById(R.id.saveButton)
    }
}
