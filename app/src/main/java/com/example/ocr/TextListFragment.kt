package com.example.ocr

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.model.DocumentModel
import com.example.ocr.model.TextModel


class TextListFragment : Fragment() {

    private var textListView: ListView? = null
    private var editText: EditText? = null
    private var saveButton: Button?=null

    private var itemsAdapter: ArrayAdapter<String>? = null
    private var datasetString:MutableList<String> = mutableListOf()

    private var db : AppDatabase? = null
    private var document : DocumentModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseClient.getInstance(requireActivity())!!.appDatabase

        val documentTitle=requireArguments().getString("documentName", "");
        document= db!!.textDao()?.getDocument(documentTitle)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_text_list, container, false)
        textListView = view.findViewById(R.id.textListView)
        setupListview()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindUIComponents(view)


        editText!!.setImeActionLabel("Save", KeyEvent.KEYCODE_ENTER);
        saveButton!!.setOnClickListener{
            val text = editText!!.text.toString()
            insertWord(text)
            datasetString!!.add(text)
            itemsAdapter!!.notifyDataSetChanged()
            editText!!.setText("")
        }

    }


    private fun setupListview() {

        datasetString = fetchAllWords()!!

        itemsAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            datasetString!!
        )
        textListView!!.adapter = itemsAdapter

        textListView!!.isLongClickable = true

        textListView!!.onItemLongClickListener =
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


    private fun fetchAllWords() = db!!.textDao()?.getAllText(document!!.id)?.map { it.text }?.toMutableList()
    private fun insertWord(text: String) {
        try{
            db!!.textDao()?.insert(TextModel(0,document!!.id,text))
        }catch (ex:Exception){
            Toast.makeText(requireContext(),"Error Occured",Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteWord(
        currentDataFromDb: MutableList<String>,
        pos: Int
    ) {
        try{
            db!!.textDao()?.deleteText(document!!.id,currentDataFromDb[pos])
        }catch (ex:Exception){
            Toast.makeText(requireContext(),"Error Occurred",Toast.LENGTH_SHORT).show()
        }
    }


    fun bindUIComponents(view: View){
        textListView=view.findViewById(R.id.textListView)
        editText=view.findViewById(R.id.textInput)
        saveButton=view.findViewById(R.id.saveButton)
    }
}