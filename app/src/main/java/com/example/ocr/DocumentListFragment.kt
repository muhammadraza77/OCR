package com.example.ocr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient

class DocumentListFragment : Fragment() {
    private var listView: ListView? = null
    private var itemsAdapter: ArrayAdapter<String>? = null
    private var datasetString:MutableList<String> = mutableListOf()

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
        datasetString = fetchAllWords()!!
        setupListview()
        return view
    }


    private fun setupListview() {
        itemsAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            datasetString!!
        )
        listView!!.adapter = itemsAdapter

        listView!!.isLongClickable = true
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val args = Bundle()
            args.putString("documentName",datasetString[i]!!)

            val fragment = TextListFragment()
            fragment.arguments = args

            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.viewPlaceholder,fragment ,"text-list-fragment")
            ft.commit()
        }
        listView!!.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, pos, id ->
                Toast.makeText(
                    requireContext(),
                    datasetString[pos] + " Removed Database",
                    Toast.LENGTH_SHORT
                ).show()
                deleteWord(datasetString, pos)
                datasetString.removeAt(pos)
                itemsAdapter!!.notifyDataSetChanged()
                true
            }
    }

    fun updateListview(documentName:String){
        datasetString.add(documentName)
        itemsAdapter!!.notifyDataSetChanged()
    }

    private fun fetchAllWords() = db!!.textDao()?.getAllDocuments()?.map { it.title }?.toMutableList()
    private fun deleteWord(
        currentDataFromDb: MutableList<String>,
        pos: Int
    ) {
        try {
            val document=db!!.textDao()?.getDocument(currentDataFromDb[pos])
            db!!.textDao()?.deleteText(document!!.id)
            db!!.textDao()?.deleteDocument(currentDataFromDb[pos])
        }catch (ex:Exception){
            Toast.makeText(requireContext(),"Error Occured",Toast.LENGTH_SHORT).show()
        }

    }

}