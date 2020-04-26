package com.manickchand.mlkitsample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val faceContoursDetection = FaceContoursDetection()

    companion object{
        const val TAG_DEBUG = "MLKITSAMPLE"
        const val CIRCLE_FACE_RADIUS = 2.0f
        var isFrontCamera = true
        var RADIO_SELECTED = 0

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView.setLifecycleOwner(this)

        cameraView.addFrameProcessor {frame ->

           recognizeFrame(frame)

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

    private fun recognizeFrame(frame: Frame){
        val fImage = convertFrameToFVI(frame)

        var bitmap = fImage.bitmap

        when(RADIO_SELECTED){
            0 -> {
                bitmap = faceContoursDetection.recognizeFace(fImage)
            }
            1 -> {
                detectText(fImage)
            }
            2 -> {
                //
            }
        }

        imageView.setImageBitmap(bitmap)
    }


    private fun convertFrameToFVI(frame: Frame): FirebaseVisionImage {

        val frameMetadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .setFormat(IMAGE_FORMAT_NV21)
            .setRotation(FirebaseVisionImageMetadata.ROTATION_270)

        //if(RADIO_SELECTED==0)frameMetadata.setFormat(frame.format)

        //back camera
        if(!isFrontCamera) frameMetadata.setRotation(FirebaseVisionImageMetadata.ROTATION_90)

        return FirebaseVisionImage.fromByteArray(frame.getData(), frameMetadata.build())
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {

            val checked = view.isChecked

            when (view.getId()) {
                R.id.radio_face ->
                    if (checked) {
                        RADIO_SELECTED = 0
                    }
                R.id.radio_text ->
                    if (checked) {
                        RADIO_SELECTED = 1
                    }
                R.id.radio_code ->
                    if (checked) {
                        RADIO_SELECTED = 2
                    }

            }
        }
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
