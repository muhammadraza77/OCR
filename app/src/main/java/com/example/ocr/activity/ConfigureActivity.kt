package com.example.ocr.activity

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.AddNewDialog
import com.example.ocr.DocumentListFragment
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.model.TextModel
import com.example.ocr.utility.SharedPreferences
import com.opencsv.CSVReader
import java.io.InputStreamReader


class ConfigureActivity : AppCompatActivity() {


    private var db : AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        db = DatabaseClient.getInstance(applicationContext)!!.appDatabase

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.viewPlaceholder, DocumentListFragment(),"document-list-fragment")
        ft.commit()

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
//                selectCSVFile1()
                return true
            }
            R.id.deleteAll ->{
//                deleteAllWord(currentDataFromDb)
//                currentDataFromDb?.clear()
//                itemsAdapter!!.notifyDataSetChanged()
                return true
            }
            R.id.addList ->{
                val dialogFragment = AddNewDialog()
                val ft = supportFragmentManager.beginTransaction()
                dialogFragment.show(ft,"dialog")

            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun deleteAllWord(currentDataFromDb: MutableList<String>?) {
        currentDataFromDb?.forEachIndexed {index,element->
            val document=db!!.textDao()?.getDocument(element)
            db!!.textDao()?.deleteText(document!!.id)
            db!!.textDao()?.deleteDocument(element)
        }
    }


}
