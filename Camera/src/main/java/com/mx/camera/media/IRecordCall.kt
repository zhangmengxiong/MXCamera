package com.mx.camera.media

import java.io.File

open class IRecordCall {
    open fun onStartPreview() = Unit
    open fun onStartRecord() = Unit
    open fun onRecordTimeTicket(time: Long) = Unit
    open fun onError(msg: String?) = Unit
    open fun onStopRecord(time: Long) = Unit
    open fun onTakePicture(file: File) = Unit
}