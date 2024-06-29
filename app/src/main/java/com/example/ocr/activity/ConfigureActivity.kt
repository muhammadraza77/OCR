package com.example.ocr.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.AddNewDialog
import com.example.ocr.DocumentListFragment
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.utility.SharedPreferences


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

}
