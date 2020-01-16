package com.mx.camera.media

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class MXSurfaceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {
    private var surfaceListener: ((SurfaceHolder?) -> Unit)? = null
    private var mSurfaceHolder: SurfaceHolder? = null

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                if (mSurfaceHolder == holder) return
                synchronized(this@MXSurfaceView) {
                    mSurfaceHolder = holder
                    surfaceListener?.invoke(holder)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                synchronized(this@MXSurfaceView) {
                    mSurfaceHolder = null
                    surfaceListener?.invoke(null)
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                synchronized(this@MXSurfaceView) {
                    mSurfaceHolder = holder
                    surfaceListener?.invoke(holder)
                }
            }
        })
    }


    fun addSurfaceChange(call: ((SurfaceHolder?) -> Unit)) {
        synchronized(this@MXSurfaceView) {
            mSurfaceHolder?.let {
                call.invoke(it)
            }
            surfaceListener = call
        }
    }
}