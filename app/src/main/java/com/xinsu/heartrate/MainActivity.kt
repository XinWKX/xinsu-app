package com.xinsu.heartrate

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
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var bpmText: TextView
    private lateinit var statusText: TextView

    private lateinit var scanButton: Button
    private lateinit var demoButton: Button
    private lateinit var exportButton: Button

    private val handler = Handler(Looper.getMainLooper())

    private var currentBpm = 72

    private var demoRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        bpmText = findViewById(R.id.bpmText)
        statusText = findViewById(R.id.statusText)

        scanButton = findViewById(R.id.scanButton)
        demoButton = findViewById(R.id.demoButton)
        exportButton = findViewById(R.id.exportButton)

        scanButton.setOnClickListener {

            Toast.makeText(
                this,
                "BLE功能开发中",
                Toast.LENGTH_SHORT
            ).show()
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

        currentBpm = Random.nextInt(65, 95)

        bpmText.text = currentBpm.toString()

        playHeartbeatSound()

        val interval = (60000f / currentBpm).toLong()

        handler.postDelayed({

            runHeartbeatLoop()

        }, interval)
    }

    private fun playHeartbeatSound() {

        Thread {

            try {

                val sampleRate = 44100

                val duration = 80

                val numSamples = duration * sampleRate / 1000

                val samples = ShortArray(numSamples)

                for (i in samples.indices) {

                    val wave = sin(
                        2.0 * Math.PI * i / (sampleRate / 85.0)
                    )

                    samples[i] = (wave * Short.MAX_VALUE * 0.18).toInt().toShort()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    samples.size * 2,
                    AudioTrack.MODE_STATIC
                )

                audioTrack.write(samples, 0, samples.size)

                audioTrack.play()

                Thread.sleep(120)

                audioTrack.release()

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }
}
