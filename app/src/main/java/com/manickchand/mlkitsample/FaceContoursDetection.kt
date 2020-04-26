package com.manickchand.mlkitsample

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.manickchand.mlkitsample.util.MyPaints


class FaceContoursDetection {

    private val detector:FirebaseVisionFaceDetector
    private val faceList = ArrayList<FirebaseVisionFace>()
    private val myPaints = MyPaints()

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.FAST) //	FAST  | ACCURATE
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS) // NO_CONTOURS | ALL_CONTOURS
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS) //NO_CLASSIFICATIONS | ALL_CLASSIFICATIONS
            .setMinFaceSize(0.15f) //default 0.1f
            .enableTracking()
            .build()

        detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

    }

    //TODO GraphicOverlay
    fun recognizeFace(firebaseImage:FirebaseVisionImage):Bitmap{

        detectFaceContours(firebaseImage)

        val bitmap = firebaseImage.bitmap
        val canvas = Canvas(bitmap)

        for(face in this.faceList){

            face.getContour(FirebaseVisionFaceContour.FACE).points.forEach {
                canvas.drawCircle(it.x, it.y, MainActivity.CIRCLE_FACE_RADIUS, myPaints.paintFace)
            }

            face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points.forEach{
                canvas.drawCircle(it.x, it.y, MainActivity.CIRCLE_FACE_RADIUS, myPaints.paintEye)
            }

            face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).points.forEach{
                canvas.drawCircle(it.x, it.y, MainActivity.CIRCLE_FACE_RADIUS, myPaints.paintEye)
            }

            face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).points.forEach{
                canvas.drawCircle(it.x, it.y, MainActivity.CIRCLE_FACE_RADIUS, myPaints.paintLip)
            }

            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).points.forEach{
                canvas.drawCircle(it.x, it.y, MainActivity.CIRCLE_FACE_RADIUS, myPaints.paintLip)
            }

            if(face.smilingProbability>0.6){
                canvas.drawText("smiling",face.boundingBox.left.toFloat(),face.boundingBox.top.toFloat(),myPaints.paintText)
            }

            canvas.drawRect(RectF(face.boundingBox), myPaints.paintRectFace)
        }

        this.faceList.clear()
        return bitmap

    }

    private fun detectFaceContours(image:FirebaseVisionImage){
        val result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->

                faceList.addAll(faces)

            }
            .addOnFailureListener { e ->
                Log.e(MainActivity.TAG_DEBUG, "Error detect faces ${e.message}")
            }
    }

    fun stopFaceContours(){
        this.detector.close()
    }

}