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
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat

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
        Log.i(TAG, "camera frame income ${inputFrame?.rgba().toString()}")
        matMap["mat1"] = inputFrame?.rgba() as Mat

        return matMap["mat1"]!!
    }

    override fun onCameraViewStopped() {
        Log.i(TAG, "camera view stopped")
        matMap.forEach { (_, mat) -> mat.release() }
        matMap.clear()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(TAG, "camera view start $width - $height")
        matMap["mat1"] = Mat(width, height, CvType.CV_8UC4)
        matMap["mat2"] = Mat(width, height, CvType.CV_8UC4)
        matMap["mat3"] = Mat(width, height, CvType.CV_8UC4)
    }

    companion object {
        fun newInstance() = JavaCameraFragment()
    }
}