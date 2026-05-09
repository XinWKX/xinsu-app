package com.xinsu.heartrate

import android.bluetooth.*
import android.content.Context
import java.util.*

class HeartRateService(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

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

    fun connect(device: BluetoothDevice) {

        bluetoothGatt = device.connectGatt(
            context,
            false,
            object : BluetoothGattCallback() {

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {

                    if (newState ==
                        BluetoothProfile.STATE_CONNECTED
                    ) {

                        gatt.discoverServices()
                    }
                }

                override fun onServicesDiscovered(
                    gatt: BluetoothGatt,
                    status: Int
                ) {

                    val service =
                        gatt.getService(
                            HEART_RATE_SERVICE_UUID
                        )

                    val characteristic =
                        service?.getCharacteristic(
                            HEART_RATE_CHARACTERISTIC_UUID
                        )

                    if (characteristic != null) {

                        gatt.readCharacteristic(
                            characteristic
                        )
                    }
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int
                ) {

                    if (
                        characteristic.uuid ==
                        HEART_RATE_CHARACTERISTIC_UUID
                    ) {

                        val heartRate =
                            value[1].toInt()

                        println(
                            "Heart Rate: $heartRate"
                        )
                    }
                }
            }
        )
    }

    fun disconnect() {

        bluetoothGatt?.disconnect()

        bluetoothGatt?.close()
    }
}
