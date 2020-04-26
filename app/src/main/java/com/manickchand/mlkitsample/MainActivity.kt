package com.manickchand.mlkitsample

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val rectList = ArrayList<Rect>()

    companion object{
        const val TAG_DEBUG = "MLKITSAMPLE"
        var isFrontCamera = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView.setLifecycleOwner(this)

        cameraView.addFrameProcessor {frame ->
            recognizeFace(frame)
            recognizeText(frame)
        }

        btnChangeCamera.setOnClickListener{
            isFrontCamera=!isFrontCamera
            Log.i(TAG_DEBUG, "isFront = $isFrontCamera")
            if(isFrontCamera){
                cameraView.facing = Facing.FRONT;
            }else{
                cameraView.facing = Facing.BACK;
            }
        }
    }

    private fun recognizeFace(frame: Frame){

        val fImage = convertFrameToFVI(frame)
        detectFaces(fImage)

        val bitmap = fImage.bitmap
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.setStrokeWidth(2f)

        for(rect in this.rectList){
            canvas.drawRect(RectF(rect), paint)
        }

        imageView.setImageBitmap(bitmap)

        this.rectList.clear()

    }

    private fun recognizeText(frame: Frame){
        val fImage = convertFrameToFVI(frame)
        detectText(fImage)
    }

    private fun convertFrameToFVI(frame: Frame): FirebaseVisionImage {

        val frameMetadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .setFormat(frame.format)
            .setRotation(FirebaseVisionImageMetadata.ROTATION_270)

        //back camera
        if(!isFrontCamera) frameMetadata.setRotation(FirebaseVisionImageMetadata.ROTATION_90)

        return FirebaseVisionImage.fromByteArray(frame.getData(), frameMetadata.build())
    }

    private fun detectFaces(image: FirebaseVisionImage) {

        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        val result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->

                for (face in faces) {
                    val bounds = face.boundingBox
                    rectList.add(bounds)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity,"Error detect faces", Toast.LENGTH_SHORT).show()
            }

    }

    private fun detectText(image: FirebaseVisionImage){

        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        val result = detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                tv_recognized.text = firebaseVisionText.text
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity,"Error detect text", Toast.LENGTH_SHORT).show()
            }
    }
}
