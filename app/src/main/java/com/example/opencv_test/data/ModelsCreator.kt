package com.example.opencv_test.data

import android.app.Activity
import android.content.Context
import android.util.Log
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val FACE_DIR = "facelib"
private const val FACE_MODEL_SMILE = "haarcascade_smile.xml"
private const val FACE_MODEL_FRONTFACE = "haarcascade_frontface_alt2.xml"
private const val byteSize = 4096

fun Activity.loadFaceLib(id: Int): CascadeClassifier? {
    try {
        val modelInputStream = resources.openRawResource(id)
        val faceDir = getDir(FACE_DIR, Context.MODE_PRIVATE)
        val faceModel = File(faceDir, FACE_MODEL_FRONTFACE)
        val modelOutputStream = FileOutputStream(faceModel)
        val buffer = ByteArray(byteSize)
        var byteRead = modelInputStream.read(buffer)
        while (byteRead != -1) {
            modelOutputStream.write(buffer, 0, byteRead)
            byteRead = modelInputStream.read(buffer)
        }

        modelInputStream.close()
        modelOutputStream.close()
        val result = CascadeClassifier(faceModel.absolutePath)
        if (!result.empty()) faceDir.delete()
        return result
    } catch (e: IOException) {
        Log.i("Activity.loadFaceLib()", "Error loading cascade face model...$e")
    }
    return null
}