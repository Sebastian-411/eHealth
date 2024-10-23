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

    // Characteristic UUIDs for the command service
    private val CAPTURE_CHARACTERISTIC = UUID.fromString("12345678-1234-5678-1234-56789abcdef2")
    private val COMMAND_SERVICE = UUID.fromString("12345678-1234-5678-1234-56789abcdef0")

    // List of characteristics to notify when they change
    private val READ_DATA_CHARACTERISTIC = UUID.fromString("12345678-1234-5678-1234-56789abcdef1")
    private val CHARACTERISTICS_TO_NOTIFY = listOf(READ_DATA_CHARACTERISTIC, CAPTURE_CHARACTERISTIC)

    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            Log.d("BluetoothGattCallback", "Connected to GATT server.")
            bluetoothGatt = gatt
            gatt?.discoverServices()
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            Log.d("BluetoothGattCallback", "Disconnected from GATT server.")
            bluetoothGatt?.close()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("BluetoothGattCallback", "Services discovered.")
            gatt?.services?.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid in CHARACTERISTICS_TO_NOTIFY) {
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
    fun sendStartSignal() {
        val characteristic = bluetoothGatt?.getService(COMMAND_SERVICE)?.getCharacteristic(CAPTURE_CHARACTERISTIC)
        characteristic?.let {
            it.value = byteArrayOf(1)
            bluetoothGatt?.writeCharacteristic(it)
            viewModel.updateSendingDataStatus(true)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendStopSignal() {
        val characteristic = bluetoothGatt?.getService(COMMAND_SERVICE)?.getCharacteristic(CAPTURE_CHARACTERISTIC)
        characteristic?.let {
            it.value = byteArrayOf(0)
            bluetoothGatt?.writeCharacteristic(it)
            viewModel.updateSendingDataStatus(false)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private fun parseCharacteristic(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
        if(bluetoothGattCharacteristic.uuid == CAPTURE_CHARACTERISTIC) {
            val updatedValue = bluetoothGattCharacteristic.value[0]
            println("Start signal received: $updatedValue")
            viewModel.updateSendingDataStatus(updatedValue == 0.toByte())
            return
        }

        val updatedValue = String(bluetoothGattCharacteristic.value, Charsets.UTF_8)
        val completeJson = messageAssembler.processReceivedData(updatedValue)
        completeJson?.let {
            println("Complete JSON received: $it")
        }
    }
}