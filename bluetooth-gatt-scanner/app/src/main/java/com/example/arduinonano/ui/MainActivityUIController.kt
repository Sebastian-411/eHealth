package com.example.arduinonano.ui

import android.content.Context
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.arduinonano.viewmodel.BluetoothDeviceViewModel
import com.example.arduinonano.MainActivity
import com.example.arduinonano.R
import com.example.arduinonano.bluetooth.BluetoothDeviceRepository
import com.example.arduinonano.utils.UIUtils.showToast

/**
 * UI controller for managing the main activity's user interface, including button clicks
 * and Bluetooth device list updates.
 *
 * @param context The context (usually the activity) that this controller is bound to.
 * @param viewModel The ViewModel that provides Bluetooth device information.
 * @param bluetoothDeviceRepository Repository to manage Bluetooth device scanning and connections.
 */
class MainActivityUIController(
    private val context: Context,
    private val viewModel: BluetoothDeviceViewModel,
    private val bluetoothDeviceRepository: BluetoothDeviceRepository
) {

    private lateinit var scanButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var deviceListView: ListView
    private lateinit var connectedDeviceName: TextView

    private lateinit var devicesAdapter: BluetoothDeviceListAdapter

    /**
     * Initializes and sets up the UI components, including button listeners and device list.
     */
    fun setupUI() {
        scanButton = (context as MainActivity).findViewById(R.id.scanButton)
        disconnectButton = context.findViewById(R.id.disconnectButton)
        deviceListView = context.findViewById(R.id.deviceListView)
        connectedDeviceName = context.findViewById(R.id.connectedDeviceName)

        devicesAdapter = BluetoothDeviceListAdapter(context, mutableListOf())
        deviceListView.adapter = devicesAdapter

        scanButton.setOnClickListener { startBluetoothScan() }
        disconnectButton.setOnClickListener { bluetoothDeviceRepository.disconnectGatt() }

        setupDeviceListListener()
        observeDeviceList()
    }

    /**
     * Sets up the click listener for the Bluetooth device list. Handles device selection
     * and GATT connection.
     */
    private fun setupDeviceListListener() {
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            bluetoothDeviceRepository.stopScan()
            val device = bluetoothDeviceRepository.getDeviceAt(position)
            bluetoothDeviceRepository.connectGatt(device)
            updateUIForConnecting()
        }
    }

    /**
     * Starts the Bluetooth scanning process.
     */
    private fun startBluetoothScan() {
        stopBluetoothScan()
        bluetoothDeviceRepository.startScan()
        showToast(context, "Scanning for devices...")
    }

    /**
     * Stops the Bluetooth scanning process.
     */
    private fun stopBluetoothScan() {
        bluetoothDeviceRepository.stopScan()
    }

    /**
     * Observes changes in the list of Bluetooth devices and updates the UI accordingly.
     */
    private fun observeDeviceList() {
        bluetoothDeviceRepository.getDevicesLiveData().observe(context as MainActivity) { devices ->
            devicesAdapter.clear()
            devices.forEach { device ->
                devicesAdapter.add(device)
            }
        }
    }

    /**
     * Updates the UI to reflect the connected state of a Bluetooth device.
     *
     * @param isConnected True if connected, false otherwise.
     */
    fun updateConnectionState(isConnected: Boolean) {
        if (isConnected) {
            showToast(context, "Connected to device")
            updateUIForConnecting()
        } else {
            showToast(context, "Disconnected from device")
            updateUIForDisconnected()
        }
    }

    /**
     * Updates the displayed Bluetooth device name in the UI.
     *
     * @param name The name of the connected Bluetooth device.
     */
    fun updateDeviceName(name: String?) {
        connectedDeviceName.text = name ?: "No device connected"
    }

    /**
     * Updates the UI when connecting to a Bluetooth device.
     */
    private fun updateUIForConnecting() {
        devicesAdapter.clear()
        scanButton.isEnabled = false
        disconnectButton.isEnabled = true
    }

    /**
     * Updates the UI when disconnected from a Bluetooth device.
     */
    private fun updateUIForDisconnected() {
        scanButton.isEnabled = true
        disconnectButton.isEnabled = false
        connectedDeviceName.text = "No device connected"
    }

    /**
     * Cleans up resources and stops any active Bluetooth scans.
     */
    fun cleanup() {
        bluetoothDeviceRepository.disconnectGatt()
        bluetoothDeviceRepository.stopScan()
    }
}
