package com.mx.camera.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.mx.camera.media.MXSurfaceView
import java.io.File

class PlayerUtil(private val context: Context) {
    private val mHandler = Handler()

    private var iPlayerCall: IPlayerCall? = null
    private var mediaPlayer: MediaPlayer? = null
    fun startPlay(mxSurfaceView: MXSurfaceView, file: File) {
        release()

        mxSurfaceView.addSurfaceChange {
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file))
            mediaPlayer?.apply {
                stop()
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDisplay(it)
                setOnCompletionListener { mp ->
                    mp.release()
                    //播放解释后再次显示播放按钮
                    iPlayerCall?.onComplete()
                    mediaPlayer = null
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    iPlayerCall?.onComplete()
                    mediaPlayer = null
                    true
                }
            }
            try {
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                iPlayerCall?.onStartPlay()
                mHandler.removeCallbacksAndMessages(null)
                mHandler.post(ticketRun)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val ticketRun: Runnable by lazy {
        object : Runnable {
            override fun run() {
                val mediaPlayer = mediaPlayer ?: return
                try {
                    if (mediaPlayer.isPlaying) {
                        val max = mediaPlayer.duration
                        val cur = mediaPlayer.currentPosition
                        iPlayerCall?.onPlayTimeTicket(max, cur)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    mHandler.postDelayed(ticketRun, 200)
                }
            }
        }
    }

    fun release() {
        try {
            mediaPlayer?.stop()
        } catch (e: java.lang.Exception) {
        }
        try {
            mediaPlayer?.reset()
        } catch (e: java.lang.Exception) {
        }
        mediaPlayer = null
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setOnPlayCall(call: IPlayerCall) {
        iPlayerCall = call
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}