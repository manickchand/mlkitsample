package com.manickchand.mlkitsample

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.manickchand.mlkitsample.util.MyPaints
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val faceList = ArrayList<FirebaseVisionFace>()

    private val myPaints = MyPaints()

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
        detectFaces(fImage)

        val bitmap = fImage.bitmap
        val canvas = Canvas(bitmap)

        for(face in this.faceList){

            for(point in face.getContour(FirebaseVisionFaceContour.FACE).points){
                canvas.drawCircle(point.x, point.y, CIRCLE_FACE_RADIUS, myPaints.paintFace)
            }
            for(point in face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points){
                canvas.drawCircle(point.x, point.y, CIRCLE_FACE_RADIUS, myPaints.paintEye)
            }
            for(point in face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).points){
                canvas.drawCircle(point.x, point.y, CIRCLE_FACE_RADIUS, myPaints.paintEye)
            }

            for(point in face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).points){
                canvas.drawCircle(point.x, point.y, CIRCLE_FACE_RADIUS, myPaints.paintLip);
            }

            for(point in face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).points){
                canvas.drawCircle(point.x, point.y, CIRCLE_FACE_RADIUS, myPaints.paintLip)
            }

            if(face.smilingProbability>0.6){
                canvas.drawText("smiling",face.boundingBox.left.toFloat(),face.boundingBox.top.toFloat(),myPaints.paintText)
            }

            canvas.drawRect(RectF(face.boundingBox), myPaints.paintRectFace)
        }

        imageView.setImageBitmap(bitmap)

        this.faceList.clear()

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
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE) //	FAST  | ACCURATE
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS) // NO_CONTOURS | ALL_CONTOURS
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS) //NO_CLASSIFICATIONS | ALL_CLASSIFICATIONS
            .setMinFaceSize(0.15f) //default 0.1f
            .enableTracking()
            .build()

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        val result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->

                faceList.addAll(faces)

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
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
