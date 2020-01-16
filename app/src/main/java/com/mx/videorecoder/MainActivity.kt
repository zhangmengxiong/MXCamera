package com.mx.videorecoder

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.mx.camera.config.CameraConfig
import com.mx.camera.RecorderActivity
import com.mx.camera.config.CameraConfigBuild
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                1
        )
        zpBtn.setOnClickListener {
            val config = CameraConfigBuild.createSimplePicConfig(this)
            val intent = Intent(this, RecorderActivity::class.java)
                    .putExtra(RecorderActivity.CONFIG, config)
            startActivityForResult(intent, 0x22)
        }
        spBtn.setOnClickListener {
            val config = CameraConfigBuild.createSimple3GPConfig(this)
            val intent = Intent(this, RecorderActivity::class.java)
                    .putExtra(RecorderActivity.CONFIG, config)
            startActivityForResult(intent, 0x22)
        }

        zp2Btn.setOnClickListener {
            val build = CameraConfigBuild(CameraConfig.TYPE_PIC)
                    .setJpegQuality(30)
                    .setExpectSize(720, 1280)
                    .setOutputFile(File(cacheDir, "aaa.jpg").absolutePath)
            val config = build.build()
            val intent = Intent(this, RecorderActivity::class.java)
                    .putExtra(RecorderActivity.CONFIG, config)
            startActivityForResult(intent, 0x22)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == 0x22) {
            val file = (data?.getSerializableExtra(RecorderActivity.RESULT_KEY) as File?) ?: return
            Toast.makeText(
                    this,
                    "${file.name} ${file.length() / 1024f} Kb",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }
}
