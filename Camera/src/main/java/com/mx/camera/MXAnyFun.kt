package com.mx.camera

val Any.TAG: String
    get() = javaClass.simpleName

fun Any.Log(log: Any) {
    System.out.println(this.TAG + " --> " + log.toString())
}