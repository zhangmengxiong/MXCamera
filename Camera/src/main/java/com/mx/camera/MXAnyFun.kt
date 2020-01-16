package com.mx.camera

val Any.TAG: String
    get() = javaClass.simpleName

fun Any.Log(log: Any) {
    if (BuildConfig.DEBUG) {
        System.out.println(this.TAG + " --> " + log.toString())
    }
}