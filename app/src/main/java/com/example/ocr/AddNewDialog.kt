package com.example.ocr

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
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
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.exception.AudioFileNotFoundException
import com.example.ocr.exception.CSVFileNotFoundException
import com.example.ocr.exception.TitleNotFoundException
import com.example.ocr.model.DocumentModel
import com.example.ocr.model.TextModel
import com.google.android.material.chip.Chip
import com.opencsv.CSVReader
import java.io.*
import java.util.*


class AddNewDialog : DialogFragment() {

    var doneButton : Button? = null
    var fileUploadChip : Chip? = null
    var audioUploadChip : Chip? = null
    var documentNameEditText : EditText? = null

    var isFileUploaded = false
    var isAudioUploaded = false

    private val ACTIVITY_CHOOSE_FILE = 101
    private val PICK_FILE = 99


    private var db : AppDatabase? = null

    private var documentForInsert : DocumentModel? = null
    private val textListForInsert : MutableList<TextModel> = mutableListOf()
    private var audioUri : Uri? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doneButton = view.findViewById(R.id.btnSaveListToDB)
        fileUploadChip = view.findViewById(R.id.selectCSV)
        audioUploadChip = view.findViewById(R.id.selectAudio)

        fileUploadChip!!.setOnClickListener{
            selectCSVFile1()
        }

        audioUploadChip!!.setOnClickListener{
            selectAudioFile()
        }

        doneButton!!.setOnClickListener{

                //save to db
                try{
                    if(documentNameEditText!!.text.isNullOrBlank()){
                        throw TitleNotFoundException()
                    }else if(!isFileUploaded){
                        throw CSVFileNotFoundException()
                    }else if(!isAudioUploaded){
                        throw AudioFileNotFoundException()
                    }
                    val audio = saveToFile( requireContext(), audioUri!!)
                    documentForInsert!!.audioId =audio
                    documentForInsert!!.title = documentNameEditText!!.text.toString()

                    val documentId=db?.textDao()?.insert(documentForInsert!!)

                    textListForInsert.forEach{it.document_id=documentId!!}

                    db?.textDao()?.insertAll(textListForInsert)


                    (parentFragmentManager.findFragmentByTag("document-list-fragment") as DocumentListFragment)
                        .updateListview(documentForInsert!!.title)

                }catch (ex: IOException){
                    Toast.makeText(requireContext(),"Invalid Audio file type",Toast.LENGTH_SHORT).show()
                }
                catch (ex:TitleNotFoundException){
                    Toast.makeText(requireContext(),"Empty title Not Allowed",Toast.LENGTH_SHORT).show()
                }
                catch (ex:CSVFileNotFoundException){
                    Toast.makeText(requireContext(),"Please Provide CSV File",Toast.LENGTH_SHORT).show()
                }
                catch (ex:AudioFileNotFoundException){
                    Toast.makeText(requireContext(),"Please Provide Audio File",Toast.LENGTH_SHORT).show()
                }
                catch (ex:Exception){
                    Toast.makeText(requireContext(),"Title Already Exist",Toast.LENGTH_SHORT).show()
                }
                dismiss()
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
            PICK_FILE->{
                if (resultCode == RESULT_OK){
                    if (data != null){
                        audioUri = data.getData();
                        isAudioUploaded = true
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

    private fun selectAudioFile(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/*"
        startActivityForResult(intent, PICK_FILE)
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