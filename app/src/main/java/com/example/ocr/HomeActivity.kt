package com.example.ocr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextRecognizer
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor


class HomeActivity : AppCompatActivity(), View.OnClickListener, ImageAnalysis.Analyzer {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    var previewView: PreviewView? = null
    private var bRecord: Button? = null
    private var bCapture: Button? = null

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        previewView = findViewById(R.id.previewView);
        bCapture = findViewById(R.id.bCapture);
        bRecord = findViewById(R.id.bRecord);

        bCapture!!.setOnClickListener(this);
        bRecord!!.setOnClickListener(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture!!.addListener(this::getRunnable,getExecutor())

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

    @SuppressLint("RestrictedApi")
    private fun startCameraX(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview: Preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(previewView!!.surfaceProvider)

        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        // Video capture use case
        videoCapture = VideoCapture.Builder()
            .setVideoFrameRate(30)
            .build()

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
            imageCapture,
            imageAnalysis
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun runTextRecognition(imageFrameProxy: ImageProxy) {
        // Replace with code from the codelab to run text recognition.
        val image: InputImage = InputImage.fromMediaImage(imageFrameProxy.image, 0)
        val recognizer: com.google.mlkit.vision.text.TextRecognizer = TextRecognition.getClient()

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                println(visionText)
                bRecord!!.text = visionText.text
                imageFrameProxy.close()
            }
            .addOnFailureListener { e ->
                imageFrameProxy.close()
                e.printStackTrace()
            }
    }

    override fun analyze(image: ImageProxy) {
        println("analyze hh")
        runTextRecognition(image)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.bCapture -> println("hello")
            R.id.bRecord -> if (bRecord!!.text === "start recording") {
                bRecord!!.text = "stop recording"
//                recordVideo()
            } else {
                bRecord!!.text = "start recording"
//                videoCapture!!.stopRecording()
            }
        }
    }

}