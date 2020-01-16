package com.mx.camera.media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.concurrent.thread

object MediaHelp {
    fun loadFirstFrameFromFile(file: File, call: ((Bitmap?) -> Unit)) {
        thread {
            var bitmap: Bitmap? = null
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(file.absolutePath)
                bitmap = mmr.frameAtTime
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    mmr.release()
                } catch (e1: Exception) {
                }
                call.invoke(bitmap)
            }
        }
    }
}