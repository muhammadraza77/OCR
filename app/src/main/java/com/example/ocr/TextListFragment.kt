package com.example.ocr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.model.DocumentModel
import com.example.ocr.model.TextModel
import com.example.ocr.utility.AudioManager
import com.opencsv.CSVReader
import java.io.*
import java.util.*


class TextListFragment : Fragment() {

    private var textListView: ListView? = null
    private var editText: EditText? = null
    private var saveButton: Button?=null

    private var itemsAdapter: ArrayAdapter<String>? = null
    private var datasetString:MutableList<String> = mutableListOf()

    private var db : AppDatabase? = null
    private var document : DocumentModel? = null

    private var ACTIVITY_CHOOSE_FILE = 101
    private val PICK_FILE = 99


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);

        db = DatabaseClient.getInstance(requireActivity())!!.appDatabase

        val documentTitle=requireArguments().getString("documentName", "");
        document= db!!.textDao()?.getDocument(documentTitle)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.text_list_menu, menu)
        true
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.uploadCSV ->{
                selectCSVFile1()
                return true
            }
            R.id.playAudio ->{
                val audioManager = AudioManager()
                val directory: File = requireActivity().cacheDir
                val file =  directory.path + File.separator + document!!.audioId
                audioManager.playAudioFile(requireContext(),file)
            }
            R.id.changeAudio -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "audio/*"
                startActivityForResult(intent, PICK_FILE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            ACTIVITY_CHOOSE_FILE -> {
                if (resultCode == AppCompatActivity.RESULT_OK){
                    try{
                        val reader = CSVReader(InputStreamReader(requireView().context.contentResolver.openInputStream(data!!.data!!)))
                        val csvRows: MutableList<Array<String>> = reader.readAll()

                        val textList = csvRows.map{
                            TextModel(0,document!!.id,it[0])
                        }

                        db!!.textDao()!!.insertAll(textList)
                        datasetString!!.addAll(textList.map { it.text })
                        itemsAdapter!!.notifyDataSetChanged()
                    }catch (ex:Exception){
                        Toast.makeText(activity,ex.message, Toast.LENGTH_SHORT).show()
                        throw ex
                    }
                }
            }
            PICK_FILE->{
                if (resultCode == Activity.RESULT_OK){
                    if (data != null){

                        val audio = saveToFile( requireContext(), data.data!!)
                        document!!.audioId =audio
                        db?.textDao()?.updateDocument(document!!)

                    }
                }
            }

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

    private fun selectCSVFile1(){

        //imp step
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
//                val uri = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
        } else {
            // for below android 11
            val intent = Intent()
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                102
            )
        }
    }

    fun saveToFile(context: Context, uri: Uri):String{

        val audioId = UUID.randomUUID().toString()
        val fileType = ".mp3"

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        val directory: File = requireActivity().cacheDir
        val destinationFilename = directory.path+File.separator+audioId+fileType
        try {
            bis = BufferedInputStream(context.getContentResolver().openInputStream(uri))
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            val buffer = ByteArray(1024)
            bis.read(buffer)
            do {
                bos.write(buffer)
            } while (bis.read(buffer) !== -1)
            bis?.close()
            bos?.close()
            return audioId+fileType
        }catch (ioe: IOException) {
            bis?.close()
            bos?.close()
            throw ioe
        }
    }

}