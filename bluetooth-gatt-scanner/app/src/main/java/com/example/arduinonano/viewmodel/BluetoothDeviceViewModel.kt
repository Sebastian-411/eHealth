package com.example.arduinonano.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel that stores and manages UI-related data for the Bluetooth device connection.
 * Provides LiveData for device name, address, and connection status.
 */
class BluetoothDeviceViewModel : ViewModel() {

    private val _sendingData = MutableLiveData(false)
    val sendingData: LiveData<Boolean> get() = _sendingData

    private val _deviceStopped = MutableLiveData(false)
    val deviceStopped: LiveData<Boolean> get() = _deviceStopped

    /**
     * Updates the sending data status in the ViewModel.
     *
     * @param sendingData True if sending data to the Bluetooth device, false otherwise.
     */
    fun updateSendingDataStatus(sendingData: Boolean) {
        _sendingData.postValue(sendingData)
    }

    /**
     * Updates the device stopped status in the ViewModel.
     *
     * @param deviceStopped True if the device has stopped, false otherwise.
     */
    fun updateDeviceStoppedStatus(deviceStopped: Boolean) {
        _deviceStopped.postValue(deviceStopped)
    }

}
