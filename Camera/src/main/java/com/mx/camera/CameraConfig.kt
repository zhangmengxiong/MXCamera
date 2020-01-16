package com.mx.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaRecorder
import android.view.WindowManager
import java.io.File
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

class CameraConfig : Serializable {
    var type: Int = TYPE_VIDEO

    var expectWidth: Int = 0
    var expectHeight: Int = 0
    var expectPreviewFps: Int = 20
    var jpegQuality: Int = 100
    var pictureFormat: Int = ImageFormat.JPEG
    var focusMode: String? = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO

    var audioSource: Int = MediaRecorder.AudioSource.MIC
    var audioEncoder: Int = MediaRecorder.AudioEncoder.DEFAULT

    var videoSource: Int = MediaRecorder.VideoSource.CAMERA
    var videoEncoder: Int = MediaRecorder.VideoEncoder.H264
    var videoOutputFormat: Int = MediaRecorder.OutputFormat.THREE_GPP
    var videoFrameRate: Int = 30
    var videoEncodingBitRate: Int = 3 * 1024 * 1024
    var maxDuration: Int = 60 * 5

    var outputFile: String? = null

    companion object {
        const val TYPE_VIDEO = 0x11
        const val TYPE_PIC = 0x12

        fun createSimple3GPConfig(context: Context, file: File? = null): CameraConfig {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val bestWidth = max(display.width, display.height)
            val bestHeight = min(display.width, display.height)
            return CameraConfig().apply {
                expectWidth = bestWidth
                expectHeight = bestHeight
                type = TYPE_VIDEO
                outputFile = file?.absolutePath
                        ?: getTempFile(context, TYPE_VIDEO).absolutePath
            }
        }

        fun createSimplePicConfig(context: Context, file: File? = null): CameraConfig {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val bestWidth = max(display.width, display.height)
            val bestHeight = min(display.width, display.height)
            return CameraConfig().apply {
                expectWidth = bestWidth
                expectHeight = bestHeight
                type = TYPE_PIC
                outputFile = file?.absolutePath
                        ?: getTempFile(context, TYPE_PIC).absolutePath
            }
        }

        private fun getTempFile(context: Context, type: Int): File {
            return when (type) {
                TYPE_VIDEO -> File(context.externalCacheDir, "${System.currentTimeMillis()}.mp4")
                else -> File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
            }
        }
    }
}