package com.xinsu.heartrate

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var bpmText: TextView

    private lateinit var statusText: TextView

    private lateinit var scanButton: Button

    private lateinit var demoButton: Button

    private lateinit var exportButton: Button

    private val handler =
        Handler(Looper.getMainLooper())

    private var currentBpm = 72

    private var demoRunning = false

    private var bluetoothAdapter:
            BluetoothAdapter? = null

    private var bleScanner:
            BluetoothLeScanner? = null

    private val scannedDevices =
        mutableListOf<String>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val bluetoothManager =
            getSystemService(
                Context.BLUETOOTH_SERVICE
            ) as BluetoothManager

        bluetoothAdapter =
            bluetoothManager.adapter

        bpmText =
            findViewById(R.id.bpmText)

        statusText =
            findViewById(R.id.statusText)

        scanButton =
            findViewById(R.id.scanButton)

        demoButton =
            findViewById(R.id.demoButton)

        exportButton =
            findViewById(R.id.exportButton)

        scanButton.setOnClickListener {

            startBleScan()
        }

        demoButton.setOnClickListener {

            if (!demoRunning) {

                startDemoMode()

            } else {

                stopDemoMode()
            }
        }

        exportButton.setOnClickListener {

            Toast.makeText(
                this,
                "日志系统开发中",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startDemoMode() {

        demoRunning = true

        statusText.text = "演示模式运行中"

        demoButton.text = "停止演示"

        runHeartbeatLoop()
    }

    private fun stopDemoMode() {

        demoRunning = false

        statusText.text = "演示模式已停止"

        demoButton.text = "演示模式"

        handler.removeCallbacksAndMessages(null)
    }

    private fun runHeartbeatLoop() {

        if (!demoRunning) return

        currentBpm =
            Random.nextInt(65, 95)

        bpmText.text =
            currentBpm.toString()

        playHeartbeatSound()

        val interval =
            (60000f / currentBpm).toLong()

        handler.postDelayed({

            runHeartbeatLoop()

        }, interval)
    }

    private fun playHeartbeatSound() {

        Thread {

            try {

                val sampleRate = 44100

                val duration = 80

                val numSamples =
                    duration * sampleRate / 1000

                val samples =
                    ShortArray(numSamples)

                for (i in samples.indices) {

                    val wave = sin(
                        2.0 * Math.PI *
                                i /
                                (sampleRate / 85.0)
                    )

                    samples[i] =
                        (
                                wave *
                                        Short.MAX_VALUE *
                                        0.18
                                ).toInt().toShort()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    samples.size * 2,
                    AudioTrack.MODE_STATIC
                )

                audioTrack.write(
                    samples,
                    0,
                    samples.size
                )

                audioTrack.play()

                Thread.sleep(120)

                audioTrack.release()

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }

    private fun startBleScan() {

        if (bluetoothAdapter == null) {

            statusText.text =
                "设备不支持蓝牙"

            return
        }

        if (!bluetoothAdapter!!.isEnabled) {

            statusText.text =
                "请先开启蓝牙"

            return
        }

        val permissions = arrayOf(

            Manifest.permission.BLUETOOTH_SCAN,

            Manifest.permission.BLUETOOTH_CONNECT,

            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missing = permissions.filter {

            ContextCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                missing.toTypedArray(),
                1001
            )

            statusText.text =
                "正在请求蓝牙权限"

            return
        }

        statusText.text =
            "正在扫描设备..."

        scannedDevices.clear()

        bleScanner =
            bluetoothAdapter!!
                .bluetoothLeScanner

        bleScanner?.startScan(scanCallback)
    }

    private val scanCallback =
        object : ScanCallback() {

            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {

                runOnUiThread {

                    val name =
                        result.device.name
                            ?: "未知设备"

                    if (!scannedDevices.contains(name)) {

                        scannedDevices.add(name)

                        statusText.text =
                            "发现设备: $name"

                        showDeviceList()
                    }
                }
            }
        }

    private fun showDeviceList() {

        val items =
            scannedDevices.toTypedArray()

        AlertDialog.Builder(this)

            .setTitle("发现的设备")

            .setItems(items) { _, which ->

                val selected =
                    scannedDevices[which]

                statusText.text =
                    "已选择设备: $selected"

                Toast.makeText(
                    this,
                    "后续将连接: $selected",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .setNegativeButton("关闭", null)

            .show()
    }
}
