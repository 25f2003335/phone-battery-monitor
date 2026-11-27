package com.example.phonebatterymonitor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {
    private lateinit var host: EditText
    private lateinit var token: EditText
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 48, 28, 24)
            setBackgroundColor(Color.rgb(247, 247, 250))
        }
        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, 18, 0, 6)
        }
        val title = TextView(this).apply {
            text = "Phone Battery Monitor"
            textSize = 24f
            setTextColor(Color.BLACK)
        }
        host = EditText(this).apply {
            hint = "Mac IP address (e.g. 192.168.1.5)"
            setSingleLine(true)
            setText(prefs.getString("host", ""))
        }
        token = EditText(this).apply {
            hint = "Shared token"
            setSingleLine(true)
            setText(prefs.getString("token", "redmi-local-2026"))
        }
        info = TextView(this).apply {
            text = "Enter the Mac's local IP address. Both devices must use the same Wi-Fi."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 18, 0, 18)
        }
        val start = Button(this).apply {
            text = "Start sharing"
            setOnClickListener {
                val h = host.text.toString().trim()
                val t = token.text.toString().trim()
                if (h.isBlank() || t.isBlank()) {
                    info.text = "Please enter the Mac IP address and token."
                    return@setOnClickListener
                }
                prefs.edit().putString("host", h).putString("token", t).apply()
                if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
                }
                val intent = Intent(this, BatteryShareService::class.java).apply {
                    action = BatteryShareService.ACTION_START
                    putExtra("host", h)
                    putExtra("token", t)
                }
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
                info.text = "Sharing started. Keep the ongoing notification active."
            }
        }
        val stop = Button(this).apply {
            text = "Stop sharing"
            setOnClickListener {
                stopService(Intent(this@MainActivity, BatteryShareService::class.java))
                info.text = "Sharing stopped."
            }
        }
        layout.addView(title)
        layout.addView(label("Mac IP address"))
        layout.addView(host, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(label("Shared token"))
        layout.addView(token, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(start)
        layout.addView(stop)
        layout.addView(info)
        setContentView(layout)
    }
}
