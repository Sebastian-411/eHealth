package com.example.arduinonano.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.example.arduinonano.viewmodel.BluetoothDeviceViewModel
import com.example.arduinonano.data.MessageAssembler
import java.util.*

class BluetoothGattCallbackHandler(
    private val viewModel: BluetoothDeviceViewModel
) : BluetoothGattCallback() {

    private val messageAssembler = MessageAssembler()
    private val MY_CHARACTERISTIC_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef1")
    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            Log.d("BluetoothGattCallback", "Connected to GATT server.")
            bluetoothGatt = gatt
            gatt?.discoverServices()
            viewModel.updateConnectionStatus(true)
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            Log.d("BluetoothGattCallback", "Disconnected from GATT server.")
            bluetoothGatt?.close()
            viewModel.updateConnectionStatus(false)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("BluetoothGattCallback", "Services discovered.")
            gatt?.services?.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid == MY_CHARACTERISTIC_UUID) {
                        gatt.setCharacteristicNotification(characteristic, true)
                    }
                }
            }
        } else {
            Log.d("BluetoothGattCallback", "Service discovery failed: $status")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("BluetoothGattCallback", "Characteristic read.")
            characteristic?.let(this::parseCharacteristic)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let(this::parseCharacteristic)
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        viewModel.updateConnectionStatus(false)
    }

    private fun parseCharacteristic(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
        val updatedValue = String(bluetoothGattCharacteristic.value, Charsets.UTF_8)
        val completeJson = messageAssembler.processReceivedData(updatedValue)
        completeJson?.let {
            println("Complete JSON received: $it")
        }
    }
}