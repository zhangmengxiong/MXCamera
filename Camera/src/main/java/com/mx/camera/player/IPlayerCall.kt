package com.mx.camera.player

open class IPlayerCall {
    open fun onStartPlay() = Unit
    open fun onComplete() = Unit
    open fun onPlayTimeTicket(maxTime: Int, curTime: Int) = Unit
}