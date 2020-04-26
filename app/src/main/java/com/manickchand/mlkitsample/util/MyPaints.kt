package com.manickchand.mlkitsample.util

import android.graphics.Color
import android.graphics.Paint

class MyPaints{

    val paintRectFace = Paint()
    val paintFace = Paint()
    val paintEye = Paint()
    val paintLip = Paint()
    val paintText = Paint()

    init{

        paintRectFace.style = Paint.Style.STROKE
        paintRectFace.strokeWidth = 2f
        paintRectFace.color = Color.RED

        paintEye.style = Paint.Style.STROKE
        paintEye.strokeWidth = 2f
        paintEye.color = Color.BLUE

        paintFace.style = Paint.Style.STROKE
        paintFace.strokeWidth = 2f
        paintFace.color = Color.GREEN

        paintLip.style = Paint.Style.STROKE
        paintLip.strokeWidth = 2f
        paintLip.color = Color.YELLOW

        paintText.style = Paint.Style.FILL_AND_STROKE
        paintText.textSize = 40f
        paintText.color = Color.CYAN

    }

}