package com.mx.camera.media

import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Handler
import android.view.SurfaceHolder
import com.mx.camera.config.CameraConfig
import com.mx.camera.Log
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RecordUtil(private val mxSurfaceView: MXSurfaceView) {
    private val mHandler = Handler()
    private lateinit var cameraConfig: CameraConfig
    private var cameraIndex = CAMERA_BACK
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null

    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    private var isCameraLocked = true

    fun setConfig(config: CameraConfig) {
        cameraConfig = config
    }

    fun switchCamera() {
        cameraIndex = if (cameraIndex == CAMERA_FRONT) CAMERA_BACK else CAMERA_FRONT
        curState = STATE_INIT
        startPreview()
    }

    private fun createCamera(holder: SurfaceHolder?): Camera {
        val camera = Camera.open(cameraIndex)
        camera.setDisplayOrientation(90)//旋转90度
        camera.setPreviewDisplay(holder)
        val params = camera.parameters
        //注意此处需要根据摄像头获取最优像素，//如果不设置会按照系统默认配置最低160x120分辨率
        val bestWidth = max(cameraConfig.expectWidth, cameraConfig.expectHeight)
        val bestHeight = min(cameraConfig.expectWidth, cameraConfig.expectHeight)

        val previewSize = SizeBiz.getPreviewSize(camera, bestWidth, bestHeight)
        val pictureSize = SizeBiz.getPictureSize(camera, bestWidth, bestHeight)
        val previewFps = SizeBiz.chooseFixedPreviewFps(camera, cameraConfig.expectPreviewFps)
        params.apply {
            previewWidth = previewSize.width
            previewHeight = previewSize.height

            setPreviewSize(previewSize.width, previewSize.height)
            setPictureSize(pictureSize.width, pictureSize.height)

            previewFps?.let {
                Log("setPreviewFpsRange ${it.first}~${it.second}")
                setPreviewFpsRange(it.first, it.second)
            }

            jpegQuality = cameraConfig.jpegQuality
            pictureFormat = cameraConfig.pictureFormat
            try {
                if (supportedFocusModes?.contains(cameraConfig.focusMode) == true) {
                    focusMode = cameraConfig.focusMode //对焦模式
                }
            } catch (e: java.lang.Exception) {
            }
        }
        camera.parameters = params
        return camera
    }

    private var maxDurationLength: Int = -1

    fun setMaxDuration(duration: Int) {
        maxDurationLength = duration
    }

    @Synchronized
    fun startPreview() {
        if (curState == STATE_PREVIEW) return

        try {
            mCamera?.apply {
                if (!isCameraLocked) {
                    lock()
                }
                isCameraLocked = true
                stopPreview()
                setPreviewCallback(null)
                release()
            }
            mCamera = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            setState(STATE_INIT)

            mxSurfaceView.addSurfaceChange {
                val camera = createCamera(it)
                mCamera = camera
                camera.startPreview()
                camera.setErrorCallback { error, _ ->
                    Log("setErrorCallback $error")
                }
                camera.setFaceDetectionListener { faces, _ ->
                    Log("FaceDetectionListener")
                }
                isCameraLocked = true
                setState(STATE_PREVIEW)
                recordCall?.onStartPreview()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mCamera?.apply {
                try {
                    if (!isCameraLocked) {
                        lock()
                    }
                    isCameraLocked = true
                } catch (e: Exception) {
                }
                try {
                    stopPreview()
                    setPreviewCallback(null)
                    release()
                } catch (e: Exception) {
                }
                mCamera = null
            }
            setState(STATE_ERROR)
        }
    }

    private var recordStartTime: Long = 0
    fun startRecord() {
        try {
            if (curState != STATE_PREVIEW) {
                startPreview()
            }
            val camera = mCamera ?: throw Exception("启动摄像头失败！")
            if (isCameraLocked) {
                camera.unlock()
            }
            isCameraLocked = false

            mMediaRecorder = MediaRecorder().apply {
                reset()
                setOnErrorListener { mr, what, extra ->
                    Log("MediaRecorder onError $what $extra")
                }
                setOnInfoListener { mr, what, extra ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        Log("OnInfoListener onError $what $extra")
                        stopRecord()
                    }
                }

                setCamera(camera)
                // 设置音频源与视频源 这两项需要放在setOutputFormat之前
                setAudioSource(cameraConfig.audioSource)
                setVideoSource(cameraConfig.videoSource)
                //设置输出格式
                setOutputFormat(cameraConfig.videoOutputFormat)
                //这两项需要放在setOutputFormat之后 IOS必须使用ACC
                setAudioEncoder(cameraConfig.audioEncoder)  //音频编码格式
                //使用MPEG_4_SP格式在华为P20 pro上停止录制时会出现
                //MediaRecorder: stop failed: -1007
                //java.lang.RuntimeException: stop failed.
                // at android.media.MediaRecorder.stop(Native Method)
                setVideoEncoder(cameraConfig.videoEncoder)  //视频编码格式

                if (cameraIndex == CAMERA_FRONT) {
                    setOrientationHint(270)
                } else {
                    setOrientationHint(90)
                }

                //设置记录会话的最大持续时间（毫秒）
                if (cameraConfig.maxDuration > 1000) {
                    setMaxDuration(cameraConfig.maxDuration)
                }
                setOutputFile(cameraConfig.outputFile)

                //设置最终出片分辨率
                setVideoSize(previewWidth, previewHeight)
                setVideoFrameRate(cameraConfig.videoFrameRate)
                setVideoEncodingBitRate(cameraConfig.videoEncodingBitRate)

                prepare()
                start()
            }
            recordStartTime = System.currentTimeMillis()
            setState(STATE_RECORDING)
            recordCall?.onStartRecord()
            mHandler.removeCallbacksAndMessages(null)
            mHandler.post(ticketRun)
        } catch (e: Exception) {
            e.printStackTrace()
            recordCall?.onError(e.message)
        }
    }

    private val ticketRun: Runnable by lazy {
        object : Runnable {
            override fun run() {
                if (curState != STATE_RECORDING) return
                try {
                    val time = abs(recordStartTime - System.currentTimeMillis())
                    recordCall?.onRecordTimeTicket(time)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    mHandler.postDelayed(ticketRun, 300)
                }
            }
        }
    }

    private var curState = STATE_INIT
    private fun setState(state: Int) {
        curState = state
    }

    fun stopRecord() {
        if (curState != STATE_RECORDING) return
        val time = abs(recordStartTime - System.currentTimeMillis())
        try {
            mMediaRecorder?.apply {
                stop()
                reset()
                release()
                mMediaRecorder = null
            }
            mCamera?.apply {
                if (!isCameraLocked) {
                    lock()
                }
                isCameraLocked = true

                stopPreview()
                setPreviewCallback(null)
                release()
                mCamera = null
            }

            setState(STATE_INIT)
            mHandler.removeCallbacksAndMessages(null)
            recordCall?.onStopRecord(time)
        } catch (e: Exception) {
            e.printStackTrace()
            if (time < 2000) {
                recordCall?.onError("录制时间过短！")
            } else {
                recordCall?.onError(e.message ?: "录制错误！")
            }
            setState(STATE_ERROR)
        }
    }

    fun requestFocus() {
        if (curState != STATE_PREVIEW) return
        try {
            mCamera?.autoFocus { success, camera -> }
        } catch (e: java.lang.Exception) {
        }
    }

    fun takePicture() {
        if (curState == STATE_TAKE_PICTURE) return
        try {
            mMediaRecorder?.apply {
                reset()
                release()
                mMediaRecorder = null
            }
            startPreview()
            setState(STATE_TAKE_PICTURE)
            val camera = mCamera ?: throw Exception("启动摄像头失败！")
            camera.takePicture(null, null, Camera.PictureCallback { data, _ ->
                Log("takePicture ${data.size}")
                mCamera?.apply {
                    lock()
                    stopPreview()
                    release()
                    mCamera = null
                }
                setState(STATE_INIT)
                if (data == null) {
                    recordCall?.onError("拍照失败！")
                    return@PictureCallback
                }
                thread {
                    try {
                        val file = File(cameraConfig.outputFile)
                        BitmapBiz.dataToBitmap(file, data, 90)
                        mHandler.post { recordCall?.onTakePicture(file) }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            })
            setState(STATE_INIT)
        } catch (e: java.lang.Exception) {
            setState(STATE_ERROR)
            e.printStackTrace()
            recordCall?.onError(e.message ?: "拍照失败！")
        }
    }

    var recordCall: IRecordCall? = null
    fun setOnRecordCall(call: IRecordCall) {
        recordCall = call
    }

    fun release() {
        setState(STATE_INIT)
        try {
            mMediaRecorder?.apply {
                stop()
                reset()
                release()
                mMediaRecorder = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mCamera?.apply {
                if (!isCameraLocked) {
                    lock()
                }
                isCameraLocked = true

                stopPreview()
                setPreviewCallback(null)
                release()
                mCamera = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val STATE_INIT = 0
        const val STATE_PREVIEW = 1
        const val STATE_RECORDING = 2
        const val STATE_TAKE_PICTURE = 3
        const val STATE_ERROR = 4

        const val CAMERA_BACK = Camera.CameraInfo.CAMERA_FACING_BACK
        const val CAMERA_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT
    }
}