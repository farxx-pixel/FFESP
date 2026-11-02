package com.esp.ffindia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val btnStart = Button(this).apply { text = "Start ESP (Shizuku)" }
        val btnStop = Button(this).apply { text = "Stop ESP" }

        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)

        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        btnStart.setOnClickListener {
            if (!ShizukuHelper.isReady()) {
                Toast.makeText(this, "Start Shizuku service first (Wireless debugging)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startForegroundService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "ESP started – non-root", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "ESP stopped", Toast.LENGTH_SHORT).show()
        }
    }
}
