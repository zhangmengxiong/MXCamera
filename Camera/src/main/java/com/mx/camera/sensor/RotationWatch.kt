package com.mx.camera.sensor

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import kotlin.math.abs


class RotationWatch(private val activity: Activity) {
    private var mSensorRotation: Int = 0
    private var eventListener: ((rotation: Int) -> Unit)? = null
    private val sensorManager by lazy {
        activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    }
    private val mAccelerometer by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun startListener(listener: ((rotation: Int) -> Unit)? = null) {
        eventListener = listener
        sensorManager.registerListener(
                sensorEventListener,
                mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun stopListener() {
        sensorManager.unregisterListener(sensorEventListener)
        eventListener = null
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent) {
            mSensorRotation = calculateSensorRotation(event.values[0], event.values[1])
            eventListener?.invoke(mSensorRotation)
        }
    }

    private fun calculateSensorRotation(x: Float, y: Float): Int {
        //x是values[0]的值，X轴方向加速度，从左侧向右侧移动，values[0]为负值；从右向左移动，values[0]为正值
        //y是values[1]的值，Y轴方向加速度，从上到下移动，values[1]为负值；从下往上移动，values[1]为正值
        //不考虑Z轴上的数据，
        if (abs(x) > 6 && abs(y) < 4) {
            return if (x > 6) {
                270
            } else {
                90
            }
        } else if (abs(y) > 6 && abs(x) < 4) {
            return if (y > 6) {
                0
            } else {
                180
            }
        }
        return 0
    }

    fun currentRotation() = mSensorRotation

    fun calculateCameraPreviewOrientation(isFront: Boolean, cameraOrientation: Int): Int {
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (isFront) {
            result = (cameraOrientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (cameraOrientation - degrees + 360) % 360
        }
        return result
    }
}