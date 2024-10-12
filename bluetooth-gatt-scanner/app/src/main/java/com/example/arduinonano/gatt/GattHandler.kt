package com.example.arduinonano.gatt

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.arduinonano.BluetoothDeviceViewModel
import java.util.*

class GattHandler (
    private val context: Context,
    private val viewModel: BluetoothDeviceViewModel
) {
    private val messageAssembler = MessageAssembler()
    private var bluetoothGatt: BluetoothGatt? = null
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val MY_CHARACTERISTIC_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef1")

    @SuppressLint("MissingPermission")
    fun connectGatt(device: BluetoothDevice) {
        viewModel.updateDeviceName(device.name ?: "Unknown")
        viewModel.updateDeviceAddress(device.address)
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        viewModel.updateConnectionStatus(false)
        Log.d("GattHandler", "Disconnected from GATT server.")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("GattHandler", "Connected to GATT server.")
                bluetoothGatt?.discoverServices()
                viewModel.updateConnectionStatus(true)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("GattHandler", "Disconnected from GATT server.")
                bluetoothGatt?.close()
                viewModel.updateConnectionStatus(false)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("GattHandler", "Services discovered.")
                gatt?.services?.forEach { service ->
                    service.characteristics.forEach { characteristic ->
                        enableNotification(gatt, characteristic)
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
                val updatedValue = String(it.value, Charsets.UTF_8)
                val completeJson = messageAssembler.processReceivedData(updatedValue)
                completeJson?.let {
                    println("Complete JSON received: $it")
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }

        @SuppressLint("MissingPermission")
        private fun readCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            gatt.readCharacteristic(characteristic)
        }
    }
}