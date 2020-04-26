package com.manickchand.mlkitsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val faceContoursDetection = FaceContoursDetection()

    companion object{
        const val TAG_DEBUG = "MLKITSAMPLE"
        const val CIRCLE_FACE_RADIUS = 2.0f
        var isFrontCamera = true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView.setLifecycleOwner(this)

        cameraView.addFrameProcessor {frame ->
            recognizeFace(frame)
            //recognizeText(frame)
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
        val bitmap = faceContoursDetection.recognizeFace(fImage)
        imageView.setImageBitmap(bitmap)
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


    private fun detectText(image: FirebaseVisionImage){

        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        val result = detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                tv_recognized.text = firebaseVisionText.text
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
