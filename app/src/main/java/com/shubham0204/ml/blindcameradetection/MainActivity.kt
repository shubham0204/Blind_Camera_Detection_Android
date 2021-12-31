package com.shubham0204.ml.blindcameradetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shubham0204.ml.blindcameradetection.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityMainBinding
    private val viewModel : UIViewModel by viewModels<UIViewModel>()
    private lateinit var frameAnalyzer : FrameAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate( layoutInflater )
        setContentView( viewBinding.root )

        frameAnalyzer = FrameAnalyzer( viewModel )

        viewModel.isCameraBlinded.observe( this , { newValue ->
            if ( newValue ) {
                viewBinding.cardView.setCardBackgroundColor( Color.YELLOW )
            }
            else {
                viewBinding.cardView.setCardBackgroundColor( Color.WHITE )
            }
        })

        viewModel.colorStdDev.observe( this , { newValue ->
            if (viewModel.isCameraBlinded.value == true) {
                viewBinding.textView.text = "Camera Blocked \n $newValue"
            }
            else {
                viewBinding.textView.text = newValue
            }

        })

        if ( checkCameraPermission() ) {
            startCameraPreview()
        }
        else {
            requestCameraPermission()
        }

        frameAnalyzer.startDetection()

    }

    private fun requestCameraPermission() {
        cameraPermissionRequestLauncher.launch( Manifest.permission.CAMERA )
    }

    private val cameraPermissionRequestLauncher = registerForActivityResult( ActivityResultContracts.RequestPermission() ) {
            isGranted ->
        if ( isGranted ) {
            startCameraPreview()
        }
        else {
            val alertDialog = MaterialAlertDialogBuilder( this ).apply {
                setTitle( "Camera Permission")
                setMessage( "The app couldn't function without the camera permission." )
                setCancelable( false )
                setPositiveButton( "ALLOW" ) { dialog, which ->
                    dialog.dismiss()
                    requestCameraPermission()
                }
                setNegativeButton( "CLOSE" ) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                create()
            }
            alertDialog.show()
        }
    }

    private fun checkCameraPermission() : Boolean {
        return ActivityCompat.checkSelfPermission( this , Manifest.permission.CAMERA ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance( this )
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview : Preview = Preview.Builder().build()
            val cameraSelector : CameraSelector = CameraSelector.Builder()
                .requireLensFacing( CameraSelector.LENS_FACING_BACK )
                .build()
            preview.setSurfaceProvider( viewBinding.previewView.surfaceProvider )
            val imageFrameAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio( AspectRatio.RATIO_4_3 )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyzer )
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview , imageFrameAnalysis )
        }, ContextCompat.getMainExecutor(this) )
    }


}