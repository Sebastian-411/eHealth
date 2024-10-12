package com.example.arduinonano.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.arduinonano.viewmodel.BluetoothDeviceViewModel

class BluetoothDeviceRepository(
    private val context: Context,
    private val viewModel: BluetoothDeviceViewModel
) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val devicesList = mutableListOf<BluetoothDevice>()
    private val devicesLiveData = MutableLiveData<List<BluetoothDevice>>()

    private lateinit var gattCallbackHandler: BluetoothGattCallbackHandler

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!devicesList.contains(it)) {
                        devicesList.add(it)
                        devicesLiveData.postValue(devicesList)
                        Log.d("BluetoothDeviceRepo", "Dispositivo encontrado: ${it.name} - ${it.address}")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        devicesList.clear()
        bluetoothAdapter.startDiscovery()
        context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            context.unregisterReceiver(receiver)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectGatt(device: BluetoothDevice) {
        viewModel.updateDeviceName(device.name ?: "Unknown")
        viewModel.updateDeviceAddress(device.address)
        gattCallbackHandler = BluetoothGattCallbackHandler(viewModel)
        device.connectGatt(context, false, gattCallbackHandler)
    }

    fun disconnectGatt() {
        gattCallbackHandler.disconnectGatt()
    }

    fun getDevicesLiveData(): LiveData<List<BluetoothDevice>> = devicesLiveData

    fun getDeviceAt(position: Int): BluetoothDevice {
        return devicesList[position]
    }
}
