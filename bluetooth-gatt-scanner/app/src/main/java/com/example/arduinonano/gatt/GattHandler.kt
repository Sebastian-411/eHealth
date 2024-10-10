package com.example.arduinonano.gatt

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*
import android.os.Environment
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Manages Bluetooth GATT connections, service discovery, and characteristic notifications.
 *
 * @param context The context in which GATT operations are executed.
 */
class GattHandler(private val context: Context) {
    val messageQueue = MessageQueue()

    private var bluetoothGatt: BluetoothGatt? = null
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") // Client Characteristic Configuration Descriptor UUID
    private val MY_CHARACTERISTIC_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef1") // Client Characteristic Configuration Descriptor UUID


    /**
     * Connects to the GATT server of the specified Bluetooth device.
     *
     * @param device The Bluetooth device to connect to.
     */
    @SuppressLint("MissingPermission")
    fun connectGatt(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    /**
     * Disconnects from the GATT server and releases resources.
     */
    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("GattHandler", "Disconnected from GATT server.")
    }

    /**
     * Callback to handle GATT events such as connection state changes and service discovery.
     */
    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("GattHandler", "Connected to GATT server.")
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("GattHandler", "Disconnected from GATT server.")
                bluetoothGatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GattHandler", "Services discovered.")

                gatt?.services?.forEach { service ->
                    Log.d("GattHandler", "Service UUID: ${service.uuid}")

                    service.characteristics.forEach { characteristic ->
                        Log.d("GattHandler", "Characteristic UUID: ${characteristic.uuid}")

                        enableNotification(gatt, characteristic)

                        // Solo lee la característica si es una que deseas leer inicialmente
                        if (characteristic.uuid == MY_CHARACTERISTIC_UUID) {
                            readCharacteristic(gatt, characteristic)
                        }
                    }
                }
            } else {
                Log.d("GattHandler", "Service discovery failed: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.let {
                    val value = it.value?.joinToString(", ") ?: "No value"
                    Log.d("GattHandler", "Characteristic ${it.uuid} read value: $value")
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

            characteristic?.let {
                val updatedValue = String(it.value, Charsets.UTF_8) // Decodificar el mensaje
                Log.d("GattHandler", "Characteristic ${it.uuid} changed value: $updatedValue")
                if (updatedValue != null) {
                    messageQueue.addMessage(updatedValue)
                }

            }
        }


        /**
         * Enables notifications for the given characteristic if supported.
         *
         * @param gatt The GATT client.
         * @param characteristic The characteristic for which to enable notifications.
         */
        @SuppressLint("MissingPermission")
        private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val status = gatt.writeDescriptor(descriptor)
                Log.d("GattHandler", "Notifications enabled for ${characteristic.uuid} with status: $status")
            } else {
                Log.d("GattHandler", "Characteristic ${characteristic.uuid} does not support notifications")
            }
        }

        /**
         * Reads the value of the specified characteristic.
         *
         * @param gatt The GATT client.
         * @param characteristic The characteristic to read.
         */
        @SuppressLint("MissingPermission")
        private fun readCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            gatt.readCharacteristic(characteristic)
        }
    }
}
