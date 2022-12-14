package com.example.ocr

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.model.DocumentModel
import com.example.ocr.model.TextModel
import com.google.android.material.chip.Chip
import com.opencsv.CSVReader
import java.io.InputStreamReader


class AddNewDialog : DialogFragment() {

    var doneButton : Button? = null
    var fileUploadChip : Chip? = null
    var audioUploadChip : Chip? = null
    var documentNameEditText : EditText? = null

    var isFileUploaded = false
    var isAudioUploaded = true
    private var ACTIVITY_CHOOSE_FILE = 101

    private var db : AppDatabase? = null

    private var documentForInsert : DocumentModel? = null
    private val textListForInsert : MutableList<TextModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseClient.getInstance(requireActivity())!!.appDatabase

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_dialog, container, false)
        documentNameEditText=view.findViewById(R.id.documentTitle)
        return view
    }

    override fun onDestroyView() {
        (parentFragmentManager.findFragmentByTag("document-list-fragment") as DocumentListFragment).updateListview()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doneButton = view.findViewById(R.id.btnSaveListToDB)
        fileUploadChip = view.findViewById(R.id.selectCSV)
        audioUploadChip = view.findViewById(R.id.selectAudio)

        fileUploadChip!!.setOnClickListener{
            selectCSVFile1()
        }

        doneButton!!.setOnClickListener{
            if(isFileUploaded && isAudioUploaded){
                //save to db
                val documentId=db?.textDao()?.insert(documentForInsert!!)
                textListForInsert.forEach{it.document_id=documentId!!}
                db?.textDao()?.insertAll(textListForInsert)

                dismiss()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            ACTIVITY_CHOOSE_FILE -> {
                if (resultCode == AppCompatActivity.RESULT_OK){
                    try{
                        val reader = CSVReader(InputStreamReader(requireView().context.contentResolver.openInputStream(data!!.data!!)))
                        val csvRows: MutableList<Array<String>> = reader.readAll()

                        documentForInsert = DocumentModel(0,documentNameEditText!!.text.toString(),"hello")

                        csvRows.forEach{
                            textListForInsert!!.add(TextModel(0,documentForInsert!!.id,it[0]))
                        }

                        isFileUploaded = true

                    }catch (ex:Exception){
                        Toast.makeText(activity,ex.message, Toast.LENGTH_SHORT).show()
                        throw ex
                    }
                }
            }

        }
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


}