package com.mx.videorecoder

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.mx.camera.CameraConfig
import com.mx.camera.RecorderActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 1)
        startActivity(Intent(this, RecorderActivity::class.java)
                .putExtra(RecorderActivity.CONFIG, CameraConfig.createSimplePicConfig(this)))
    }
}
