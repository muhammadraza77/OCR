package com.example.ocr.activity

//import com.google.android.gms.vision.text.TextRecognizer
//import com.google.mlkit.vision.text.TextRecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.ocr.R
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
import com.example.ocr.constant.StorageKey
import com.example.ocr.utility.GraphicOverlay
import com.example.ocr.utility.SharedPreferences
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor


class HomeActivity : AppCompatActivity(),  ImageAnalysis.Analyzer {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    var previewView: PreviewView? = null
    private var settingButton: ImageButton? = null
    private var detectedTextView: TextView? = null
    private var mGraphicOverlay: GraphicOverlay? = null


    var db : AppDatabase? = null
    var wordList: Set<String> = emptySet()
    var isCaseSensitive: Boolean? = null
    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        if (checkPermission()) {
            //main logic or main code
            bindUIComponents()

            db = DatabaseClient.getInstance(applicationContext)!!.appDatabase
            populateWordsFromDatabase()

            sharedPreferences = SharedPreferences(applicationContext)
            isCaseSensitive = sharedPreferences!!.readValue(StorageKey.IsCaseSensitive)

            setupCameraX()

            settingButton!!.setOnClickListener {
                startActivity(Intent(applicationContext, ConfigureActivity::class.java));
            }

//        } else {
//            requestPermission();
//        }

    }
    override fun onResume() {
        super.onResume()
        populateWordsFromDatabase()
        isCaseSensitive = sharedPreferences!!.readValue(StorageKey.IsCaseSensitive)
    }

    private fun setupCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture!!.addListener(this::getRunnable, getExecutor())
    }

    private fun getRunnable(){

        try {
            val cameraProvider = cameraProviderFuture!!.get()
            startCameraX(cameraProvider)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun getExecutor(): Executor? {
        return ContextCompat.getMainExecutor(this)
    }

    private fun bindUIComponents() {
        previewView = findViewById(R.id.previewView);
        detectedTextView = findViewById(R.id.detectedTextView);
        settingButton = findViewById(R.id.settingButton);
        mGraphicOverlay = findViewById(R.id.graphic_overlay)
    }

    private fun populateWordsFromDatabase() {
        wordList = db!!.textDao()!!.getAll().map { it.text }.toSet()
    }

    @SuppressLint("RestrictedApi")
    private fun startCameraX(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview: Preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(previewView!!.surfaceProvider)

        // Image analysis use case
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(getExecutor()!!, this)

        //bind to lifecycle:
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun runTextRecognition(imageFrameProxy: ImageProxy) {
        // Replace with code from the codelab to run text recognition.
        val image: InputImage = InputImage.fromMediaImage(imageFrameProxy.image!!, imageFrameProxy.imageInfo.rotationDegrees)
        val recognizer: TextRecognizer = TextRecognition.getClient()

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                processTextRecognitionResult(visionText)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }.addOnCompleteListener { imageFrameProxy.close() }
    }

    override fun analyze(image: ImageProxy) {

        runTextRecognition(image)

    }

    private fun processTextRecognitionResult(texts: Text) {

        val blocks = texts.textBlocks

        if (blocks.size == 0) {
            detectedTextView!!.text = ""
            mGraphicOverlay!!.clear()
            return
        }
        mGraphicOverlay!!.clear()

        val detectedText = if(isCaseSensitive!!) checkTextInDbCaseSensitive(texts.text) else checkTextInDbCaseFree(texts.text)
        if(detectedText.isNotEmpty()){
            detectedTextView!!.text = detectedText.joinToString(separator = "\n")
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        }
    }

    private fun checkTextInDbCaseFree(text: String):Set<String> = wordList.filter {
        it.uppercase() in text.uppercase()
    }.toSet()

    private fun checkTextInDbCaseSensitive(text: String):Set<String> = wordList.filter {
        it in text
    }.toSet()


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            200
        )
    }
    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()

                // main logic
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel(
                            "You need to allow access permissions"
                        ) { dialog, which ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission()
                            }
                        }
                    }
                }
            }
        }
    }
}