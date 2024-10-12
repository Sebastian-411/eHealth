package com.example.arduinonano

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothDeviceViewModel : ViewModel() {
    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String> get() = _deviceName

    private val _deviceAddress = MutableLiveData<String>()
    val deviceAddress: LiveData<String> get() = _deviceAddress

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun updateDeviceName(name: String) {
        _deviceName.postValue(name)
    }

    fun updateDeviceAddress(address: String) {
        _deviceAddress.postValue(address)
    }

    fun updateConnectionStatus(isConnected: Boolean) {
        _isConnected.postValue(isConnected)
    }
}