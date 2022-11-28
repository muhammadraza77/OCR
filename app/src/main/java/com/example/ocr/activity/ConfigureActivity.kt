package com.example.ocr.activity

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.model.TextModel
import com.example.ocr.utility.SharedPreferences
import com.opencsv.CSVReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader


class ConfigureActivity : AppCompatActivity() {
    private var toolbar: Toolbar?= null
    private var listView: ListView? = null
    private var editText: EditText? = null
    private var saveButton:Button?=null
    private var ACTIVITY_CHOOSE_FILE = 101
    private var db : AppDatabase? = null
    private var currentDataFromDb: MutableList<String>? = mutableListOf()

    private var itemsAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        bindUIComponents()
//        setActionBar(toolbar)
//        toolbar!!.inflateMenu(R.menu.menu_main)


        db = DatabaseClient.getInstance(applicationContext)!!.appDatabase
        currentDataFromDb = fetchAllWords()

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
        when (item.itemId) {
            R.id.uploadCSV ->{
                selectCSVFile1()
                return true
            }
            R.id.deleteAll ->{
                deleteAllWord(currentDataFromDb)
                currentDataFromDb?.clear()
                itemsAdapter!!.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            ACTIVITY_CHOOSE_FILE -> {
                if (resultCode == RESULT_OK){
                    try{
                        val reader = CSVReader(InputStreamReader(contentResolver.openInputStream(data!!.data!!)))
                        val myEntries: MutableList<Array<String>> = reader.readAll()

                        myEntries.forEach{
                            insertWord(it[0])
                            currentDataFromDb!!.add(it[0])
                        }
                        itemsAdapter!!.notifyDataSetChanged()

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
    private fun deleteAllWord(currentDataFromDb: MutableList<String>?) {
        currentDataFromDb?.forEachIndexed {index,element->
            db!!.textDao()?.delete(element)
        }
    }

    private fun selectCSVFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_FILE)
    }
    private fun selectCSVFile1(){

        //imp step
        if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                //choosing csv file
                val intent = Intent()
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select CSV File "), 101)
            } else {
                //getting permission from user
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
        } else {
            // for below android 11
            val intent = Intent()
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.WRITE_EXTERNAL_STORAGE),
                102
            )
        }
    }

    private fun bindUIComponents() {
        listView = findViewById(R.id.textListView)
        editText = findViewById(R.id.textInput)
        saveButton = findViewById(R.id.saveButton)
    }
}
