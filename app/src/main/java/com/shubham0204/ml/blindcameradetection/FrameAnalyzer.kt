package com.shubham0204.ml.blindcameradetection

import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream

class FrameAnalyzer(private val viewModel : UIViewModel ) : ImageAnalysis.Analyzer {


    private val stdDevOp = TensorProcessor.Builder()
        .add( StdDevOp() )
        .build()

    override fun analyze(image: ImageProxy) {
        Log.e( "APP" , "started" )
        CoroutineScope( Dispatchers.Default ).launch {
            processImage( image )
        }
    }

    private suspend fun processImage( image : ImageProxy ) = withContext( Dispatchers.Default ) {
        val bitmap = imageToBitmap( image.image!! , image.imageInfo.rotationDegrees )
        val tensorImage = TensorImage.fromBitmap( bitmap )
        val output = stdDevOp.process( tensorImage.tensorBuffer ).floatArray
        withContext( Dispatchers.Main ) {
            viewModel.colorStdDev.value = output.contentToString()
        }
        image.close()
        Log.e( "APP" , output.contentToString() )
        Log.e( "APP" , "ended" )
    }

    // Convert android.media.Image to android.graphics.Bitmap
    // See the SO answer -> https://stackoverflow.com/a/44486294/10878733
    fun imageToBitmap(image : Image, rotationDegrees : Int ): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val yuv = out.toByteArray()
        var output = BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
        output = rotateBitmap( output , rotationDegrees.toFloat() )
        return flipBitmap( output )
    }

    // Rotate the given `source` by `degrees`.
    // See this SO answer -> https://stackoverflow.com/a/16219591/10878733
    fun rotateBitmap( source: Bitmap , degrees : Float ): Bitmap {
        val matrix = Matrix()
        matrix.postRotate( degrees )
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix , false )
    }


    // Flip the given `Bitmap` horizontally.
    // See this SO answer -> https://stackoverflow.com/a/36494192/10878733
    fun flipBitmap( source: Bitmap ): Bitmap {
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

}