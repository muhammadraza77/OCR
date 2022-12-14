package com.example.ocr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient

class DocumentListFragment : Fragment() {
    private var listView: ListView? = null
    private var itemsAdapter: ArrayAdapter<String>? = null
    private var dataset:MutableList<String> = mutableListOf()

    private var db : AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseClient.getInstance(requireActivity())!!.appDatabase
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_document_list, container, false)
        listView = view.findViewById(R.id.documentListView)
        dataset = fetchAllWords()!!
        setupListview(dataset)
        return view
    }


    private fun setupListview(currentDataFromDb: MutableList<String>?) {
        itemsAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            currentDataFromDb!!
        )
        listView!!.adapter = itemsAdapter

        listView!!.isLongClickable = true
        listView!!.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, pos, id -> // TODO Auto-generated method stub
//                Toast.makeText(
//                    applicationContext,
//                    currentDataFromDb[pos] + " Removed Database",
//                    Toast.LENGTH_SHORT
//                ).show()
//                deleteWord(currentDataFromDb, pos)
//                currentDataFromDb.removeAt(pos)
//                itemsAdapter!!.notifyDataSetChanged()
                true
            }
    }

    fun updateListview(){
        dataset = fetchAllWords()!!
        itemsAdapter!!.notifyDataSetChanged()
    }

    private fun fetchAllWords() = db!!.textDao()?.getAllDocuments()?.map { it.title }?.toMutableList()

}