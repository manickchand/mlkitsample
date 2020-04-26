package com.manickchand.mlkitsample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
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

        when(RADIO_SELECTED){
            0 -> {
                val bitmap = faceContoursDetection.recognizeFace(fImage)
                imageView.setImageBitmap(bitmap)
            }
            1 -> {
                detectText(fImage)
            }
            2 -> {
                detectBarCode(fImage)
            }
        }

    }


    private fun convertFrameToFVI(frame: Frame): FirebaseVisionImage {

        val frameMetadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .setFormat(IMAGE_FORMAT_NV21) //format to text and qrcode
            .setRotation(FirebaseVisionImageMetadata.ROTATION_270)

        //formate to face detector
        if(RADIO_SELECTED==0)frameMetadata.setFormat(frame.format)

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
                        imageView.visibility = View.VISIBLE
                    }
                R.id.radio_text ->
                    if (checked) {
                        RADIO_SELECTED = 1
                        imageView.visibility = View.GONE
                    }
                R.id.radio_code ->
                    if (checked) {
                        RADIO_SELECTED = 2
                        imageView.visibility = View.GONE
                    }
            }

            Log.i(TAG_DEBUG, "RADIO_SELECTED = $RADIO_SELECTED")
        }
    }


    private fun detectText(image: FirebaseVisionImage){

        val detectorText = FirebaseVision.getInstance().onDeviceTextRecognizer

        val result = detectorText.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                tv_recognized.text = firebaseVisionText.text
            }
            .addOnFailureListener { e ->
                Log.e(TAG_DEBUG, "Error detect text ${e.message}")
            }
    }

    private fun detectBarCode(image: FirebaseVisionImage){

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        val detectorQrcode = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(options)

        val result = detectorQrcode.detectInImage(image)
            .addOnSuccessListener { barcodes ->

                //TODO more than one
                barcodes.forEach{code ->
                    tv_recognized.text = code.rawValue
                }

            }
            .addOnFailureListener {
                Log.e(TAG_DEBUG, "Error detect qrcode ${it.message}")
            }
    }
}
