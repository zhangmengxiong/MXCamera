package com.mx.camera.media

import android.hardware.Camera
import com.mx.camera.Log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object SizeBiz {
    fun getPreviewSize(camera: Camera, bestWidth: Int, bestHeight: Int): CameraSize {
        val bestSize = findBestSize(bestWidth, bestHeight, camera.parameters.supportedPreviewSizes)
        Log("Preview最佳宽度 ${bestSize?.width} 最佳高度 ${bestSize?.height}")
        return bestSize ?: CameraSize().apply {
            width = 720
            height = 1280
        }
    }

    fun getPictureSize(camera: Camera, bestWidth: Int, bestHeight: Int): CameraSize {
        val bestSize = findBestSize(bestWidth, bestHeight, camera.parameters.supportedPictureSizes)
        Log("Picture最佳宽度 ${bestSize?.width} 最佳高度 ${bestSize?.height}")
        return bestSize ?: CameraSize().apply {
            width = 720
            height = 1280
        }
    }

    private fun findBestSize(bestWidth: Int, bestHeight: Int, list: List<Camera.Size>): CameraSize? {
        val sizeList = list.mapNotNull {
//            Log("${it.width}x${it.height}")
            CameraSize.createFromSize(it)
        }
        if (sizeList.isEmpty() || bestWidth <= 0 || bestHeight <= 0) {
            return null
        }
        val bestRatio = bestWidth / bestHeight.toFloat()
        val ratioGroup = sizeList.groupBy { it.getRatio() }
        val br = ratioGroup.keys.minBy { abs(it - bestRatio) } ?: return null
        val bestList = ratioGroup[br] ?: return null
        var bestSize = bestList.filter { it.width >= bestWidth }.minBy { abs(it.width - bestWidth) + abs(it.height - bestHeight) }
        if (bestSize == null) {
            bestSize = bestList.minBy { abs(it.width - bestWidth) + abs(it.height - bestHeight) }
        }
        return bestSize
    }

    class CameraSize {
        var width: Int = 0
        var height: Int = 0

        fun getRatio() = width.toFloat() / height

        companion object {
            fun createFromSize(size: Camera.Size): CameraSize? {
                val w = max(size.width, size.height)
                val h = min(size.width, size.height)
                if (w <= 0 || h <= 0) return null
                return CameraSize().apply {
                    width = w
                    height = h
                }
            }
        }
    }

    /**
     * 选择合适的FPS
     * @param camera
     * @param expectedFps 期望的FPSsupportedPreviewFpsRange
     * @return
     */
    fun chooseFixedPreviewFps(camera: Camera, expectedFps: Int): Pair<Int, Int>? {
        try {
            val supportedFps = camera.parameters.supportedPreviewFpsRange.sortedBy { it[0] }
            supportedFps.firstOrNull { it[0] == it[1] && it[0] == expectedFps * 1000 }?.let {
                return Pair(it[0], it[1])
            }
            for (arr in supportedFps) {
                val start = arr[0]
                val end = arr[1]
                if (start == expectedFps * 1000) {
                    return Pair(start, end)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}