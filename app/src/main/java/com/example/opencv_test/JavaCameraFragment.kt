package com.example.opencv_test

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.opencv_test.data.convertToHSV
import com.example.opencv_test.data.detectFace
import com.example.opencv_test.data.drawFaceRectangle
import com.example.opencv_test.data.loadFaceLib
import com.example.opencv_test.databinding.FragmentJavaCameraBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class JavaCameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener2 {
    private val TAG = "|${this.javaClass.simpleName}|"

    private var _binding: FragmentJavaCameraBinding? = null
    private val binding: FragmentJavaCameraBinding
        get() = checkNotNull(_binding)


    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var loaderCallback: BaseLoaderCallback

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it }

    private var faceDetector: CascadeClassifier? = null

    private lateinit var inputMat: Mat
    private lateinit var outputMat: Mat

    private var mode = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJavaCameraBinding.inflate(inflater).apply { _binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().checkSelfPermission(CAMERA) != PERMISSION_GRANTED)
            permissionLauncher.launch(
                arrayOf(
                    CAMERA,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                )
            )


        binding.cameraJcv.setOnLongClickListener {
            mode = if (mode == 0) 1 else 0
            true
        }

        cameraBridgeViewBase = binding.cameraJcv
        cameraBridgeViewBase.visibility = SurfaceView.VISIBLE
        cameraBridgeViewBase.setCvCameraViewListener(this)
        cameraBridgeViewBase.setMaxFrameSize(200,200)

        loaderCallback = object : BaseLoaderCallback(requireActivity()) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        cameraBridgeViewBase.enableView()
                        faceDetector = requireActivity().loadFaceLib(R.raw.haarcascade_frontalface_alt2)
                        if (faceDetector == null || faceDetector!!.empty()) faceDetector = null
                    }
                    else -> super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) Log.i(TAG, "install failed")
        else {
            Log.i(TAG, "install success [OK]")
            loaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        }
    }

    override fun onDestroy() {
        inputMat.release()
        outputMat.release()
        cameraBridgeViewBase.disableView()
        _binding = null
        super.onDestroy()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        return if (mode == 0) inputFrame!!.convertToHSV()
        else inputFrame!!.detectFace()
            .apply { outputMat = first; inputMat = second }
            .let { faceDetector!!.drawFaceRectangle(outputMat, inputMat) }
    }

    override fun onCameraViewStopped() {
        Log.i(TAG, "camera view stopped")
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(TAG, "camera view start $width - $height")
        inputMat = Mat(width, height, CvType.CV_8UC4)
        outputMat = Mat(width, height, CvType.CV_8UC4)
    }

    private fun String.log() = Log.i(TAG, this)

    companion object {


        fun newInstance() = JavaCameraFragment()

    }
}

