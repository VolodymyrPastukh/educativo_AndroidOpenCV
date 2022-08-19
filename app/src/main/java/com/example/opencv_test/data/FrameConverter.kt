package com.example.opencv_test.data

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier


fun CameraBridgeViewBase.CvCameraViewFrame.convertToHSV(): Mat {
    val inputMat = Mat(rgba().width(), rgba().height(), CvType.CV_16UC4)
    val outputMat = Mat(rgba().width(), rgba().height(), CvType.CV_16UC4)
    Imgproc.cvtColor(rgba(), inputMat, Imgproc.COLOR_RGB2HSV)
    Core.inRange(
        inputMat,
        Scalar(45.0, 20.0, 10.0),
        Scalar(75.0, 255.0, 255.0),
        outputMat
    )

    return outputMat
}

fun CameraBridgeViewBase.CvCameraViewFrame.detectFace(): Pair<Mat,Mat> {
    val outputMat = rgba()
    var inputMat = Mat()
    Imgproc.cvtColor(outputMat, inputMat, Imgproc.COLOR_RGB2GRAY)
    inputMat = get480Image(inputMat)
    return outputMat to inputMat
}

fun CascadeClassifier.drawFaceRectangle(
    outputMat: Mat,
    grayMat: Mat,
    ratio: Double = 1.0
): Mat {
    val faceRects = MatOfRect()
    detectMultiScale(
        grayMat,
        faceRects
    )

    for (rect in faceRects.toArray()) {
        var x = 0.0
        var y = 0.0
        var w = 0.0
        var h = 0.0

        if (ratio.equals(1.0)) {
            x = rect.x.toDouble()
            y = rect.y.toDouble()
            w = x + rect.width
            h = y + rect.height
        } else {
            x = rect.x.toDouble() / ratio
            y = rect.y.toDouble() / ratio
            w = x + (rect.width / ratio)
            h = y + (rect.height / ratio)
        }

        Imgproc.rectangle(
            outputMat,
            Point(x, y),
            Point(w, h),
            Scalar(255.0, 0.0, 0.0)
        )
    }

    return outputMat
}

fun ratioTo480(src: Size): Double {
    val w = src.width
    val h = src.height
    val heightMax = 480
    var ratio: Double = 0.0

    if (w > h) {
        if (w < heightMax) return 1.0
        ratio = heightMax / w
    } else {
        if (h < heightMax) return 1.0
        ratio = heightMax / h
    }

    return ratio
}

fun get480Image(src: Mat): Mat {
    val imageSize = Size(src.width().toDouble(), src.height().toDouble())
    val ratio = ratioTo480(imageSize)

    if (ratio.equals(1.0)) return src

    val dstSize = Size(imageSize.width*ratio, imageSize.height*ratio)
    val dst = Mat()
    Imgproc.resize(src, dst, dstSize)
    return dst
}