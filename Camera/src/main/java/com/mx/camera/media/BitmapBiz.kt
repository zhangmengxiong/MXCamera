package com.mx.camera.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


object BitmapBiz {

    @Throws(Exception::class)
    fun dataToBitmap(file: File, data: ByteArray, degrees: Int) {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val newBitmap = rotateBitmap(bitmap, degrees)!!
        file.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(file))
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()
    }

    fun saveBitmap(file: File, bitmap: Bitmap) {
        file.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(file))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    fun getBitmapDegree(path: String?): Int {
        var degree = 0
        try { // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(path)
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return degree
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     *
     * @param bitmap  原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    private fun rotateBitmap(bitmap: Bitmap?, degrees: Int): Bitmap? {
        if (degrees == 0 || null == bitmap) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.setRotate(degrees.toFloat(), bitmap.width / 2f, bitmap.height / 2f)
        val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return bmp
    }
}