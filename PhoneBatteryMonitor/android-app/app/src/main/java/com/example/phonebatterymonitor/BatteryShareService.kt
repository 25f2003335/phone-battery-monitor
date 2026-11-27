package com.example.phonebatterymonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class BatteryShareService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var host = ""
    private var token = ""
    private val task = object : Runnable {
        override fun run() {
            sendReading()
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        host = intent?.getStringExtra("host") ?: getSharedPreferences("settings", MODE_PRIVATE).getString("host", "") ?: ""
        token = intent?.getStringExtra("token") ?: getSharedPreferences("settings", MODE_PRIVATE).getString("token", "") ?: ""
        startForeground(NOTIFICATION_ID, notification())
        handler.removeCallbacks(task)
        handler.post(task)
        return START_STICKY
    }

    private fun notification(): Notification {
        val channelId = "battery_share"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Battery sharing", NotificationManager.IMPORTANCE_LOW))
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else Notification.Builder(this)
        return builder.setContentTitle("Phone battery sharing is on")
            .setContentText("Sending battery status to your Mac over Wi-Fi")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .build()
    }

    private fun sendReading() {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        if (level < 0 || scale <= 0 || host.isBlank()) return
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val body = JSONObject().apply {
            put("level", (level * 100 / scale).coerceIn(0, 100))
            put("charging", status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
            put("plugged", plugged)
            put("temperature_c", if (temp >= 0) temp / 10.0 else JSONObject.NULL)
            put("voltage_v", if (voltage > 0) voltage / 1000.0 else JSONObject.NULL)
        }
        thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL("http://$host:8765/api/update").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("X-Phone-Token", token)
                }
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                connection.responseCode
            } catch (_: Exception) {
                // The dashboard will show the last received reading; next poll retries.
            } finally {
                connection?.disconnect()
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(task)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.phonebatterymonitor.START"
        const val ACTION_STOP = "com.example.phonebatterymonitor.STOP"
        private const val NOTIFICATION_ID = 42
    }
}
