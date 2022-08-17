package com.example.opencv_test

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.opencv_test.databinding.FragmentJavaCameraBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class JavaCameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener2 {
    private val TAG = "|${this.javaClass.simpleName}|"

    private var _binding: FragmentJavaCameraBinding? = null
    private val binding: FragmentJavaCameraBinding
        get() = checkNotNull(_binding)


    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var loaderCallback: BaseLoaderCallback

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { it }

    private val matMap = mutableMapOf<String, Mat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentJavaCameraBinding.inflate(inflater).apply { _binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().checkSelfPermission(CAMERA) != PERMISSION_GRANTED)
            permissionLauncher.launch(CAMERA)


        cameraBridgeViewBase = binding.cameraJcv
        cameraBridgeViewBase.visibility = SurfaceView.VISIBLE
        cameraBridgeViewBase.setCvCameraViewListener(this)

        loaderCallback = object : BaseLoaderCallback(requireActivity()) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> cameraBridgeViewBase.enableView()
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
        cameraBridgeViewBase.disableView()
        _binding = null
        super.onDestroy()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
//        val output = inputFrame!!.convertToHSV()
        return inputFrame!!.convertToHSV()
    }

    private fun CameraBridgeViewBase.CvCameraViewFrame.convertToHSV(): Mat {
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

    override fun onCameraViewStopped() {
        Log.i(TAG, "camera view stopped")
        matMap.forEach { (_, mat) -> mat.release() }
        matMap.clear()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(TAG, "camera view start $width - $height")

    }

    companion object {
        fun newInstance() = JavaCameraFragment()
    }
}