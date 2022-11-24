package com.example.ocr.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.model.TextModel
import com.example.ocr.utility.SharedPreferences
import com.opencsv.CSVReader
import java.io.FileReader


class ConfigureActivity : AppCompatActivity() {
    private var toolbar: Toolbar?= null
    private var listView: ListView? = null
    private var editText: EditText? = null
    private var saveButton:Button?=null
    private var ACTIVITY_CHOOSE_FILE = 100
    private var db : AppDatabase? = null

    private var itemsAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        bindUIComponents()
//        setActionBar(toolbar)
//        toolbar!!.inflateMenu(R.menu.menu_main)


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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val sharedPreferences = SharedPreferences(applicationContext)
        val menuItem = menu!!.findItem(R.id.caseSwitch)
        val mySwitch = menuItem.actionView as SwitchCompat
        menuItem.isChecked = sharedPreferences.readValue(StorageKey.IsCaseSensitive)
        mySwitch.isChecked = sharedPreferences.readValue(StorageKey.IsCaseSensitive)

        mySwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->

            sharedPreferences.writeKeyValue(StorageKey.IsCaseSensitive,isChecked)
            val textMsg = if(isChecked)"Application will detect case sensitive" else "Application will not detect case sensitive"
            Toast.makeText(this,textMsg,Toast.LENGTH_SHORT).show()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("hello")
        when (item.itemId) {
            R.id.uploadCSV ->{
                selectCSVFile()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            ACTIVITY_CHOOSE_FILE -> {
                if (resultCode == RESULT_OK){
                    try{
                        val fileName = data?.data?.path?.split(":")?.get(1)
                        val reader = CSVReader(FileReader(fileName))
                        val myEntries: List<*> = reader.readAll()

                        println(myEntries)

                    }catch (ex:Exception){
                        Toast.makeText(this,ex.message,Toast.LENGTH_SHORT).show()
                        throw ex
                    }
                }
            }

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

    private fun selectCSVFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_FILE)
    }

    private fun bindUIComponents() {
        listView = findViewById(R.id.textListView)
        editText = findViewById(R.id.textInput)
        saveButton = findViewById(R.id.saveButton)
//        toolbar = findViewById(R.id.toolbar)
    }
}
