package com.wstxda.toolkit.services.sensors

import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.view.Surface
import kotlin.math.roundToInt

fun SensorEvent.getAzimuthDegrees(rotation: Int): Float {
    val rotationMatrix = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
    val (azimuth, _, _) = getOrientation(rotationMatrix, rotation)
    return (Math.toDegrees(azimuth.toDouble()).roundToInt() + 360) % 360f
}

private fun getOrientation(rotationMatrix: FloatArray, rotation: Int): FloatArray {
    val remappedRotationMatrix = FloatArray(9)
    val axisX = SensorManager.AXIS_X
    val axisY = SensorManager.AXIS_Y

    when (rotation) {
        Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, axisX, axisY, remappedRotationMatrix
        )

        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, axisY, SensorManager.AXIS_MINUS_X, remappedRotationMatrix
        )

        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_MINUS_X,
            SensorManager.AXIS_MINUS_Y,
            remappedRotationMatrix
        )

        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_MINUS_Y, axisX, remappedRotationMatrix
        )
    }

    val orientation = FloatArray(3)
    SensorManager.getOrientation(remappedRotationMatrix, orientation)
    return orientation
}