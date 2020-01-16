package com.mx.camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.mx.camera.media.IRecordCall
import com.mx.camera.media.RecordUtil
import com.mx.camera.player.IPlayerCall
import com.mx.camera.player.PlayerUtil
import kotlinx.android.synthetic.main.activity_recorder.*
import java.io.File
import kotlin.math.max

class RecorderActivity : Activity() {
    private val cameraConfig: CameraConfig by lazy {
        (intent.getSerializableExtra(CONFIG) as CameraConfig?)
                ?: CameraConfig.createSimple3GPConfig(this)
    }
    private val maxDuration: Int by lazy { cameraConfig.maxDuration }
    private val file: File by lazy { File(cameraConfig.outputFile) }

    private val recordUtil: RecordUtil by lazy { RecordUtil(surfaceView) }
    private val playerUtil: PlayerUtil by lazy { PlayerUtil(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)
        initView()
        initIntent()
    }

    private fun initIntent() {
        Log("maxDuration = $maxDuration")
        recordUtil.setConfig(cameraConfig)
        recordUtil.setMaxDuration(maxDuration)
    }

    override fun onStart() {
        super.onStart()
        recordUtil.startPreview()
    }

    override fun onStop() {
        recordUtil.release()
        super.onStop()
    }

    private fun initView() {
        cancelBtn.setOnClickListener { finish() }
        surfaceView.setOnClickListener {
            recordUtil.requestFocus()
        }

        recordUtil.setOnRecordCall(object : IRecordCall() {
            override fun onStartPreview() {
                progressBar.visibility = View.GONE
                okBtn.visibility = View.GONE
                deleteBtn.visibility = View.GONE
                playBtn.visibility = View.GONE
                switchCameraBtn.visibility = View.VISIBLE
                switchCameraBtn.isEnabled = true
                Log("onStartPreview")
            }

            override fun onTakePicture(file: File) {
                Log("onTakePicture ${file.absolutePath} ${file.length() / 1024f} Kb")
                if (file.exists()) {
                    okBtn.visibility = View.VISIBLE
                    deleteBtn.visibility = View.VISIBLE
                }
                switchCameraBtn.visibility = View.GONE
                progressBar.visibility = View.GONE
            }

            override fun onStartRecord() {
                switchCameraBtn.visibility = View.GONE
                if (maxDuration > 0) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.max = maxDuration
                    progressBar.progress = maxDuration
                }
                Log("onStartRecord")
            }

            override fun onStopRecord(time: Long) {
                Log("onStopRecord $time ${file.absolutePath}  ${file.length() / 1024f} Kb")
                if (file.exists()) {
                    playBtn.visibility = View.VISIBLE
                    okBtn.visibility = View.VISIBLE
                    deleteBtn.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
            }

            override fun onRecordTimeTicket(time: Long) {
                if (maxDuration > 0) {
                    progressBar.progress = max(maxDuration - time.toInt(), 0)
                }
                Log("onRecordTimeTicket $time")
            }

            override fun onError(msg: String?) {
                Toast.makeText(this@RecorderActivity, msg ?: "录制错误！", Toast.LENGTH_SHORT).show()
                Log("onError $msg")
            }
        })
        playerUtil.setOnPlayCall(object : IPlayerCall() {
            override fun onStartPlay() {
                progressBar.visibility = View.VISIBLE
            }

            override fun onComplete() {
                playBtn.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }

            override fun onPlayTimeTicket(maxTime: Int, curTime: Int) {
                progressBar.max = maxTime
                progressBar.progress = curTime
            }
        })

        deleteBtn.setOnClickListener {
            if (playerUtil.isPlaying()) {
                playerUtil.release()
            }
            recordUtil.startPreview()
        }
        okBtn.setOnClickListener {
            if (file.exists()) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(RESULT_KEY, file.absoluteFile)
                })
            }
            finish()
        }
        playBtn.setOnClickListener {
            playBtn.visibility = View.GONE
            playerUtil.startPlay(surfaceView, file)
        }
        switchCameraBtn.setOnClickListener {
            switchCameraBtn.isEnabled = false
            recordUtil.switchCamera()
            recordUtil.startPreview()
        }

        when (cameraConfig.type) {
            CameraConfig.TYPE_VIDEO -> {
                modTxv.text = "长按拍摄"
                startBtn.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        recordUtil.startRecord()
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        recordUtil.stopRecord()
                    }
                    true
                }
            }
            CameraConfig.TYPE_PIC -> {
                modTxv.text = "轻触拍照"
                startBtn.setOnClickListener {
                    recordUtil.takePicture()
                }
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        recordUtil.release()
        playerUtil.release()
        super.onDestroy()
    }

    companion object {
        const val CONFIG = "config_camera"

        const val DURATION = "MAX_DURATION"
        const val RESULT_KEY = "result_file"
    }
}