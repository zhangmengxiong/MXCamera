package com.mx.camera

val Any.TAG: String
    get() = javaClass.simpleName

fun Any.Log(log: Any) {
    if (BuildConfig.DEBUG) {
        System.out.println(this.TAG + " --> " + log.toString())
    }
}

fun Int.toTicketTime(): String {
    var minute = "${this / 60}"
    var second = "${this % 60}"
    if (minute.length < 2) {
        minute = "0$minute"
    }
    if (second.length < 2) {
        second = "0$second"
    }
    return "$minute:$second"
}