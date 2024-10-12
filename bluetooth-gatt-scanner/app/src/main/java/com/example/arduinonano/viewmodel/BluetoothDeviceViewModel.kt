package com.example.arduinonano.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel that stores and manages UI-related data for the Bluetooth device connection.
 * Provides LiveData for device name, address, and connection status.
 */
class BluetoothDeviceViewModel : ViewModel() {
    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String> get() = _deviceName

    private val _deviceAddress = MutableLiveData<String>()
    val deviceAddress: LiveData<String> get() = _deviceAddress

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected

    /**
     * Updates the device name stored in the ViewModel.
     *
     * @param name The name of the Bluetooth device.
     */
    fun updateDeviceName(name: String) {
        _deviceName.postValue(name)
    }

    /**
     * Updates the device address stored in the ViewModel.
     *
     * @param address The address of the Bluetooth device.
     */
    fun updateDeviceAddress(address: String) {
        _deviceAddress.postValue(address)
    }

    /**
     * Updates the connection status in the ViewModel.
     *
     * @param isConnected True if connected to the Bluetooth device, false otherwise.
     */
    fun updateConnectionStatus(isConnected: Boolean) {
        _isConnected.postValue(isConnected)
    }
}
