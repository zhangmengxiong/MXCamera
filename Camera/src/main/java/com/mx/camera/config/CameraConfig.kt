package com.mx.camera.config

import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaRecorder
import java.io.Serializable

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

    var maxDuration: Int = 0
    var outputFile: String? = null

    companion object {
        const val TYPE_VIDEO = 0x11
        const val TYPE_PIC = 0x12
    }
}