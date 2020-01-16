package com.mx.camera.config

import android.content.Context
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Build
import android.view.WindowManager
import com.mx.camera.Log
import java.io.File
import kotlin.math.max
import kotlin.math.min

class CameraConfigBuild(type: Int) {
    private val cameraConfig = CameraConfig()

    init {
        if (type !in arrayOf(CameraConfig.TYPE_PIC, CameraConfig.TYPE_VIDEO)) {
            throw IllegalArgumentException("非法参数")
        }
        cameraConfig.type = type
    }

    /**
     * 预期的拍摄/录制宽高参数
     */
    fun setExpectSize(w: Int, h: Int): CameraConfigBuild {
        if (w <= 0 || h <= 0) throw IllegalArgumentException("非法参数")
        cameraConfig.expectWidth = min(w, h)
        cameraConfig.expectHeight = max(w, h)
        return this
    }

    /**
     * 预期的预览fps，会根据设置的fps动态适配最佳fps
     */
    fun setExpectPreviewFps(fps: Int): CameraConfigBuild {
        if (fps <= 1) throw IllegalArgumentException("非法参数,fps不能小于1")
        cameraConfig.expectPreviewFps = fps
        return this
    }

    /**
     * 图片质量
     */
    fun setJpegQuality(quality: Int): CameraConfigBuild {
        if (quality <= 0 || quality > 100) throw IllegalArgumentException("非法参数,quality取值范围为1~100")
        cameraConfig.jpegQuality = quality
        return this
    }

    /**
     * 焦点模式
     */
    fun setFocusMode(mod: String): CameraConfigBuild {
        val list = arrayOf(
                Camera.Parameters.FOCUS_MODE_AUTO,
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                Camera.Parameters.FOCUS_MODE_EDOF,
                Camera.Parameters.FOCUS_MODE_FIXED,
                Camera.Parameters.FOCUS_MODE_INFINITY,
                Camera.Parameters.FOCUS_MODE_MACRO
        )
        if (mod !in list) throw IllegalArgumentException("非法参数,mod取值范围为$list")
        cameraConfig.focusMode = mod
        return this
    }

    /**
     * 音频源
     */
    fun setAudioSource(source: Int): CameraConfigBuild {
        val list = arrayListOf(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.DEFAULT,
                MediaRecorder.AudioSource.CAMCORDER,
                MediaRecorder.AudioSource.REMOTE_SUBMIX,
                MediaRecorder.AudioSource.VOICE_CALL,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.VOICE_DOWNLINK,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.VOICE_UPLINK
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.add(MediaRecorder.AudioSource.UNPROCESSED)
        }
        if (source !in list) throw IllegalArgumentException("非法参数")
        cameraConfig.audioSource = source
        return this
    }

    /**
     * 音频编码
     */
    fun setAudioEncoder(encoder: Int): CameraConfigBuild {
        val list = arrayListOf(
                MediaRecorder.AudioEncoder.DEFAULT,
                MediaRecorder.AudioEncoder.AAC,
                MediaRecorder.AudioEncoder.AAC_ELD,
                MediaRecorder.AudioEncoder.HE_AAC,
                MediaRecorder.AudioEncoder.AMR_NB,
                MediaRecorder.AudioEncoder.AMR_WB
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            list.add(MediaRecorder.AudioEncoder.VORBIS)
        }
        if (encoder !in list) throw IllegalArgumentException("非法参数")
        cameraConfig.audioEncoder = encoder
        return this
    }

    /**
     * 视频录制源
     */
    fun setVideoSource(source: Int): CameraConfigBuild {
        val list = arrayListOf(
                MediaRecorder.VideoSource.CAMERA,
                MediaRecorder.VideoSource.DEFAULT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            list.add(MediaRecorder.VideoSource.SURFACE)
        }
        if (source !in list) throw IllegalArgumentException("非法参数")
        cameraConfig.videoSource = source
        return this
    }

    /**
     * 视频输出编码格式
     */
    fun setVideoEncoder(encoder: Int): CameraConfigBuild {
        val list = arrayListOf(
                MediaRecorder.VideoEncoder.DEFAULT,
                MediaRecorder.VideoEncoder.H263,
                MediaRecorder.VideoEncoder.H264,
                MediaRecorder.VideoEncoder.MPEG_4_SP
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.add(MediaRecorder.VideoEncoder.HEVC)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            list.add(MediaRecorder.VideoEncoder.VP8)
        }
        if (encoder !in list) throw IllegalArgumentException("非法参数")
        cameraConfig.videoEncoder = encoder
        return this
    }

    /**
     * 视频输出格式
     */
    fun setVideoOutputFormat(outputFormat: Int): CameraConfigBuild {
        val list = arrayListOf(
                MediaRecorder.OutputFormat.DEFAULT,
                MediaRecorder.OutputFormat.AAC_ADTS,
                MediaRecorder.OutputFormat.AMR_WB,
                MediaRecorder.OutputFormat.AMR_NB,
                MediaRecorder.OutputFormat.RAW_AMR,
                MediaRecorder.OutputFormat.MPEG_4
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            list.add(MediaRecorder.OutputFormat.MPEG_2_TS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            list.add(MediaRecorder.OutputFormat.WEBM)
        }
        if (outputFormat !in list) throw IllegalArgumentException("非法参数")
        cameraConfig.videoOutputFormat = outputFormat
        return this
    }

    /**
     * 视频帧数
     */
    fun setVideoFrameRate(rate: Int): CameraConfigBuild {
        if (rate <= 10) throw IllegalArgumentException("非法参数")
        cameraConfig.videoFrameRate = rate
        return this
    }

    /**
     * 视频比特率
     */
    fun setVideoEncodingBitRate(rate: Int): CameraConfigBuild {
        if (rate <= 1024) throw IllegalArgumentException("非法参数")
        cameraConfig.videoEncodingBitRate = rate
        return this
    }

    /**
     * 录制最长时长
     * 如果不限时长，则设置为0
     */
    fun setMaxDuration(duration: Int): CameraConfigBuild {
        if (duration < 0) throw IllegalArgumentException("非法参数")
        cameraConfig.maxDuration = duration
        return this
    }

    /**
     * 设置录制文件，需要全路径
     */
    fun setOutputFile(path: String): CameraConfigBuild {
        val file = File(path)
        val parent = file.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
//        if (parent.exists()) throw IllegalArgumentException("非法参数")
        cameraConfig.outputFile = file.absolutePath
        return this
    }

    fun build(): CameraConfig {
        return cameraConfig
    }

    companion object {
        fun createSimple3GPConfig(context: Context, file: File? = null): CameraConfig {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val bestWidth = max(display.width, display.height)
            val bestHeight = min(display.width, display.height)
            return CameraConfig().apply {
                expectWidth = bestWidth
                expectHeight = bestHeight
                type = CameraConfig.TYPE_VIDEO
                outputFile = file?.absolutePath
                        ?: getTempFile(
                                context,
                                CameraConfig.TYPE_VIDEO
                        ).absolutePath
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
                type = CameraConfig.TYPE_PIC
                outputFile = file?.absolutePath
                        ?: getTempFile(
                                context,
                                CameraConfig.TYPE_PIC
                        ).absolutePath
            }
        }

        private fun getTempFile(context: Context, type: Int): File {
            return when (type) {
                CameraConfig.TYPE_VIDEO -> File(
                        context.externalCacheDir,
                        "${System.currentTimeMillis()}.mp4"
                )
                else -> File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
            }
        }
    }
}