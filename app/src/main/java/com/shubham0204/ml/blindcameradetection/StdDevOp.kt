package com.shubham0204.ml.blindcameradetection

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.pow
import kotlin.math.sqrt

class StdDevOp() : TensorOperator {

    override fun apply(p0: TensorBuffer?): TensorBuffer {
        val buffer = p0!!.floatArray
        val red = FloatArray( buffer.size / 3 )
        val blue = FloatArray( buffer.size / 3 )
        val green = FloatArray( buffer.size / 3 )
        for ( i in buffer.indices step 3 ) {
            red[ i / 3 ] = buffer[ i ]
            green[ ( i / 3 ) ] = buffer[ i + 1 ]
            blue[ ( i / 3 )  ] = buffer[ i + 2 ]
        }
        val rMean = red.average()
        val bMean = blue.average()
        val gMean = green.average()
        val rStd = sqrt(( red.map{ it.pow( 2 ) }.sum() / red.size.toFloat() ) - rMean.pow( 2 ))
        val gStd = sqrt(( green.map{ it.pow( 2 ) }.sum() / green.size.toFloat() ) - gMean.pow( 2 ))
        val bStd = sqrt(( blue.map{ it.pow( 2 ) }.sum() / blue.size.toFloat() ) - bMean.pow( 2 ))
        val output = TensorBuffer.createFixedSize( intArrayOf( 1 , 3 ) , DataType.FLOAT32 )
        output.loadArray( floatArrayOf( rStd.toFloat() , gStd.toFloat() , bStd.toFloat() ) )
        return output
    }

}