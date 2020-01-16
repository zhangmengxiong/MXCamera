package com.mx.camera.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.mx.camera.Log
import com.mx.camera.R
import com.mx.camera.toTicketTime
import kotlinx.android.synthetic.main.layout_mx_record_ticket.view.*

class RecordTicketView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_mx_record_ticket, this, true)
        gravity = Gravity.CENTER
        orientation = VERTICAL
    }

    private var animatorSet: AnimatorSet? = null
    fun startTicket() {
        animatorSet?.end()
        animatorSet = AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(tagImg, "scaleX", 1f, 0.8f, 1f);//后几个参数是放大的倍数
            val scaleY = ObjectAnimator.ofFloat(tagImg, "scaleY", 1f, 0.8f, 1f);
            scaleX.repeatCount = ValueAnimator.INFINITE;//永久循环
            scaleY.repeatCount = ValueAnimator.INFINITE;
            duration = 1000 //时间
            play(scaleX)?.with(scaleY) //两个动画同时开始
            start() //开始
        }
    }

    fun stopTicket() {
        animatorSet?.end()
        animatorSet = null
    }

    fun setTime(second: Int) {
        Log("setTime $second")
        timeTxv.text = second.toTicketTime()
    }
}