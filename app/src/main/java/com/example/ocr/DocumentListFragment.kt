package com.example.ocr

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.utility.SharedPreferences

class DocumentListFragment : Fragment() {
    private var listView: ListView? = null
    private var itemsAdapter: ArrayAdapter<String>? = null
    private var datasetString:MutableList<String> = mutableListOf()

    private var db : AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
        db = DatabaseClient.getInstance(requireActivity())!!.appDatabase
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.document_list_menu, menu)

        val sharedPreferences = SharedPreferences(requireContext())
        val menuItem = menu!!.findItem(R.id.caseSwitch)
        val mySwitch = menuItem.actionView as SwitchCompat
        menuItem.isChecked = sharedPreferences.readValue(StorageKey.IsCaseSensitive)
        mySwitch.isChecked = sharedPreferences.readValue(StorageKey.IsCaseSensitive)

        mySwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->

            sharedPreferences.writeKeyValue(StorageKey.IsCaseSensitive,isChecked)
            val textMsg = if(isChecked)"Application will detect case sensitive" else "Application will not detect case sensitive"
            Toast.makeText(requireContext(),textMsg,Toast.LENGTH_SHORT).show()
        }

        true
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteAll ->{
                deleteAllWord(datasetString)
                datasetString?.clear()
                itemsAdapter!!.notifyDataSetChanged()
                return true
            }
            R.id.addList ->{
                val dialogFragment = AddNewDialog()
                val ft = parentFragmentManager.beginTransaction()
                dialogFragment.show(ft,"dialog")
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun deleteAllWord(currentDataFromDb: MutableList<String>?) {
        currentDataFromDb?.forEachIndexed {index,element->
            val document=db!!.textDao()?.getDocument(element)
            db!!.textDao()?.deleteText(document!!.id)
            db!!.textDao()?.deleteDocument(element)
        }
    }

}