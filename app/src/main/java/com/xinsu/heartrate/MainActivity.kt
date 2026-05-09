package com.xinsu.heartrate

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var bpmText: TextView
    private lateinit var statusText: TextView
    private lateinit var scanButton: Button
    private lateinit var demoButton: Button
    private lateinit var exportButton: Button
    private lateinit var settingsButton: Button
    private lateinit var ecgView: EcgView

    private val handler =
        Handler(Looper.getMainLooper())

    private var currentBpm = 72

    private var demoRunning = false

    private var soundEnabled = true

    private var waveEnabled = true

    private var bluetoothAdapter:
            BluetoothAdapter? = null

    private var bleScanner:
            BluetoothLeScanner? = null

    private var bluetoothGatt:
            BluetoothGatt? = null

    private var lastDevice:
            BluetoothDevice? = null

    private var audioTrack: AudioTrack? = null

    private var bpmAnimator:
            ObjectAnimator? = null

    private val scannedDevices =
        mutableListOf<BluetoothDevice>()

    companion object {

        val HEART_RATE_SERVICE_UUID: UUID =
            UUID.fromString(
                "0000180d-0000-1000-8000-00805f9b34fb"
            )

        val HEART_RATE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString(
                "00002a37-0000-1000-8000-00805f9b34fb"
            )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(null)

        window.statusBarColor =
            Color.BLACK

        setContentView(R.layout.activity_main)

        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(this)
        )

        startService(
            Intent(
                this,
                HeartRateService::class.java
            )
        )

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

        settingsButton =
            findViewById(R.id.settingsButton)

        ecgView =
            findViewById(R.id.ecgView)

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

            try {

                val file = java.io.File(
                    filesDir,
                    "crash_log.txt"
                )

                if (file.exists()) {

                    val text = file.readText()

                    AlertDialog.Builder(this)

                        .setTitle("崩溃日志")

                        .setMessage(text)

                        .setPositiveButton(
                            "关闭",
                            null
                        )

                        .show()

                } else {

                    Toast.makeText(
                        this,
                        "暂无崩溃日志",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "读取日志失败",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        settingsButton.setOnClickListener {

            showSettingsDialog()
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

        if (waveEnabled) {

            ecgView.setHeartRate(
                currentBpm
            )
        }

        if (soundEnabled) {

            playHeartbeatSound()
        }

        startBpmAnimation()

        val interval =
            (60000f / currentBpm).toLong()

        handler.postDelayed({

            runHeartbeatLoop()

        }, interval)
    }

    private fun playHeartbeatSound() {

        try {

            val sampleRate = 44100

            val durationMs = 45

            val samplesCount =
                sampleRate * durationMs / 1000

            val samples =
                ShortArray(samplesCount)

            for (i in samples.indices) {

                val envelope =
                    1.0 - (
                            i.toDouble() /
                                    samples.size
                            )

                val wave =
                    sin(
                        2.0 *
                                Math.PI *
                                140.0 *
                                i /
                                sampleRate
                    )

                samples[i] = (
                        wave *
                                envelope *
                                Short.MAX_VALUE *
                                0.22
                        ).toInt().toShort()
            }

            if (audioTrack == null) {

                val bufferSize =
                    AudioTrack.getMinBufferSize(

                        sampleRate,

                        AudioFormat.CHANNEL_OUT_MONO,

                        AudioFormat.ENCODING_PCM_16BIT
                    )

                audioTrack = AudioTrack(

                    AudioManager.STREAM_MUSIC,

                    sampleRate,

                    AudioFormat.CHANNEL_OUT_MONO,

                    AudioFormat.ENCODING_PCM_16BIT,

                    bufferSize,

                    AudioTrack.MODE_STREAM
                )

                audioTrack?.play()
            }

            audioTrack?.write(
                samples,
                0,
                samples.size
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun startBpmAnimation() {

        bpmAnimator?.cancel()

        val scaleX =
            PropertyValuesHolder.ofFloat(

                "scaleX",

                1f,
                1.06f,
                1f
            )

        val scaleY =
            PropertyValuesHolder.ofFloat(

                "scaleY",

                1f,
                1.06f,
                1f
            )

        bpmAnimator = ObjectAnimator.ofPropertyValuesHolder(

            bpmText,

            scaleX,
            scaleY
        )

        bpmAnimator?.duration = 260

        bpmAnimator?.interpolator =
            LinearInterpolator()

        bpmAnimator?.start()
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

                try {

                    runOnUiThread {

                        val device =
                            result.device

                        if (!scannedDevices.contains(device)) {

                            scannedDevices.add(device)

                            statusText.text =
                                "发现设备: ${
                                    device.name ?: "未知设备"
                                }"

                            showDeviceList()
                        }
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        }

    private fun showDeviceList() {

        val names =
            scannedDevices.map {

                it.name ?: "未知设备"

            }.toTypedArray()

        AlertDialog.Builder(this)

            .setTitle("发现的设备")

            .setItems(names) { _, which ->

                val device =
                    scannedDevices[which]

                connectToDevice(device)
            }

            .setNegativeButton("关闭", null)

            .show()
    }

    private fun connectToDevice(
        device: BluetoothDevice
    ) {

        lastDevice = device

        statusText.text =
            "正在连接: ${
                device.name ?: "未知设备"
            }"

        bluetoothGatt =
            device.connectGatt(
                this,
                false,
                gattCallback
            )
    }

    private val gattCallback =
        object : BluetoothGattCallback() {

            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int
            ) {

                try {

                    runOnUiThread {

                        if (newState ==
                            BluetoothProfile.STATE_CONNECTED
                        ) {

                            statusText.text =
                                "设备已连接"

                            gatt.discoverServices()

                        } else if (
                            newState ==
                            BluetoothProfile.STATE_DISCONNECTED
                        ) {

                            statusText.text =
                                "设备已断开"

                            handler.postDelayed({

                                lastDevice?.let {

                                    connectToDevice(it)
                                }

                            }, 3000)
                        }
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            override fun onServicesDiscovered(
                gatt: BluetoothGatt,
                status: Int
            ) {

                try {

                    val service =
                        gatt.getService(
                            HEART_RATE_SERVICE_UUID
                        )

                    val characteristic =
                        service?.getCharacteristic(
                            HEART_RATE_CHARACTERISTIC_UUID
                        )

                    if (characteristic != null) {

                        gatt.setCharacteristicNotification(
                            characteristic,
                            true
                        )
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {

                try {

                    if (
                        characteristic.uuid ==
                        HEART_RATE_CHARACTERISTIC_UUID
                    ) {

                        val flag =
                            characteristic.properties

                        val format =
                            if (flag and 0x01 != 0)
                                BluetoothGattCharacteristic.FORMAT_UINT16
                            else
                                BluetoothGattCharacteristic.FORMAT_UINT8

                        val heartRate =
                            characteristic.getIntValue(
                                format,
                                1
                            ) ?: 0

                        runOnUiThread {

                            currentBpm = heartRate

                            bpmText.text =
                                heartRate.toString()

                            if (waveEnabled) {

                                ecgView.setHeartRate(
                                    heartRate
                                )
                            }

                            statusText.text =
                                "实时心率监测中"

                            if (soundEnabled) {

                                playHeartbeatSound()
                            }

                            startBpmAnimation()
                        }
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        }

    private fun showSettingsDialog() {

        val view = layoutInflater.inflate(

            R.layout.dialog_settings,
            null
        )

        val soundSwitch =
            view.findViewById<android.widget.Switch>(
                R.id.soundSwitch
            )

        val waveSwitch =
            view.findViewById<android.widget.Switch>(
                R.id.waveSwitch
            )

        soundSwitch.isChecked =
            soundEnabled

        waveSwitch.isChecked =
            waveEnabled

        soundSwitch.setOnCheckedChangeListener {

                _, isChecked ->

            soundEnabled = isChecked
        }

        waveSwitch.setOnCheckedChangeListener {

                _, isChecked ->

            waveEnabled = isChecked
        }

        AlertDialog.Builder(this)

            .setView(view)

            .setPositiveButton(
                "关闭",
                null
            )

            .show()
    }

    override fun onRequestPermissionsResult(

        requestCode: Int,

        permissions: Array<out String>,

        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(

            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 1001) {

            if (
                grantResults.all {

                    it ==
                            PackageManager.PERMISSION_GRANTED
                }
            ) {

                startBleScan()

            } else {

                statusText.text =
                    "蓝牙权限被拒绝"
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        handler.removeCallbacksAndMessages(
            null
        )

        bluetoothGatt?.disconnect()

        bluetoothGatt?.close()

        audioTrack?.release()

        audioTrack = null
    }
}
