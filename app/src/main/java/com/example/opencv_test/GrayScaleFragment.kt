package com.example.opencv_test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.opencv_test.databinding.FragmentGrayScaleBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


class GrayScaleFragment : Fragment() {

    private val TAG = "|${this.javaClass.simpleName}|"

    private var _binding: FragmentGrayScaleBinding? = null
    private val binding: FragmentGrayScaleBinding
        get() = checkNotNull(_binding)

    private lateinit var loaderCallback: BaseLoaderCallback

    private val intentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setImageFromGallery(result.data?.data)
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loaderCallback = object : BaseLoaderCallback(requireActivity()) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> Log.i(TAG, "status success")
                    else -> super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentGrayScaleBinding.inflate(inflater).apply { _binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        galleryBtn.setOnClickListener { openGallery() }
        grayBtn.setOnClickListener { previewIv.convertToGray() }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) Log.i(TAG, "install failed")
        else {
            Log.i(TAG, "install success [OK]")
            loaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intentResultLauncher.launch(intent)
    }

    private fun setImageFromGallery(uri: Uri?) = with(binding) {
        uri?.let {
            val imageBitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    requireActivity().contentResolver,
                    it
                )
            )
            previewIv.setImageBitmap(imageBitmap)
        }
    }

    private fun ImageView.convertToGray() {
        val rgba = Mat()
        val grayMat = Mat()

        val options = BitmapFactory.Options()
        options.inDither = false
        options.inSampleSize = 4

        val bitmap = this.drawable.toBitmap()
        val confBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = bitmap.width
        val height = bitmap.height

        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Utils.bitmapToMat(confBitmap, rgba)
        Imgproc.cvtColor(rgba, grayMat, Imgproc.COLOR_RGB2GRAY)

        Utils.matToBitmap(grayMat, grayBitmap)

        setImageBitmap(grayBitmap)
    }

    companion object {
        fun newInstance() = GrayScaleFragment()
    }
}