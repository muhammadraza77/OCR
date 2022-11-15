package com.example.ocr.activity

//import com.google.android.gms.vision.text.TextRecognizer
//import com.google.mlkit.vision.text.TextRecognition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.ocr.utility.GraphicOverlay
import com.example.ocr.R
import com.example.ocr.utility.TextGraphic
import com.example.ocr.config.AppDatabase
import com.example.ocr.config.DatabaseClient
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindUIComponents()

        db = DatabaseClient.getInstance(applicationContext)!!.appDatabase
        populateWordsFromDatabase()

        setupCameraX()

        settingButton!!.setOnClickListener {
            startActivity(Intent(applicationContext, ConfigureActivity::class.java));
        };

    }
    override fun onResume() {
        super.onResume()
        populateWordsFromDatabase()
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

        val detectedText = checkTextInDb(texts.text)
        detectedTextView!!.text = detectedText.toString()

        for (block in blocks) {
            for (line in block.lines) {
                val lineText = line.text
                if(detectedText.isNotEmpty()){
                    val textGraphic: GraphicOverlay.Graphic =
                        TextGraphic(
                            mGraphicOverlay,
                            line
                        )
                    mGraphicOverlay!!.add(textGraphic)
                }

            }
        }
    }

    private fun checkTextInDb(text: String):Set<String> = wordList.filter { it in text }.toSet()

}