package com.example.arduinonano.ui

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Button
import android.widget.ListView
import com.example.arduinonano.viewmodel.BluetoothDeviceViewModel
import com.example.arduinonano.MainActivity
import com.example.arduinonano.R
import com.example.arduinonano.bluetooth.BluetoothDeviceRepository
import com.example.arduinonano.utils.UIUtils.showToast

/**
 * UI controller for managing the main activity's user interface, including handling Bluetooth device connections,
 * disconnections, and list updates.
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

    private lateinit var connectNewDeviceButton: Button
    private lateinit var deviceListView: ListView
    private lateinit var connectedDevicesListView: ListView
    private lateinit var devicesAdapter: BluetoothDeviceListAdapter
    private lateinit var connectedDevicesAdapter: ConnectedDeviceAdapter

    private val connectedDevices: MutableList<BluetoothDevice> = mutableListOf()

    /**
     * Initializes the UI components and sets up listeners for button clicks and list updates.
     */
    fun setupUI() {
        connectNewDeviceButton = (context as MainActivity).findViewById(R.id.connectNewDeviceButton)
        connectedDevicesListView = context.findViewById(R.id.connectedDevicesListView)
        deviceListView = context.findViewById(R.id.deviceListView)

        devicesAdapter = BluetoothDeviceListAdapter(context, mutableListOf())
        connectedDevicesAdapter = ConnectedDeviceAdapter(context, connectedDevices, this::disconnectDevice)

        deviceListView.adapter = devicesAdapter
        connectedDevicesListView.adapter = connectedDevicesAdapter

        connectNewDeviceButton.setOnClickListener { startBluetoothScan() }

        setupDeviceListListener()
        observeDeviceList()
        observeConnectedDevices()
    }

    /**
     * Sets up the listener for the device list view, allowing users to connect to devices from the list.
     */
    private fun setupDeviceListListener() {
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = bluetoothDeviceRepository.getDeviceAt(position)

            if (!connectedDevices.contains(device)) {
                bluetoothDeviceRepository.connectGatt(device)
                connectedDevices.add(device)
                connectedDevicesAdapter.notifyDataSetChanged()

                bluetoothDeviceRepository.stopScan()
                devicesAdapter.clear()
            } else {
                showToast(context, "Dispositivo ya conectado.")
            }
        }
    }

    /**
     * Starts a new Bluetooth scan and filters out already connected devices from the scanned list.
     */
    private fun startBluetoothScan() {
        bluetoothDeviceRepository.stopScan()
        bluetoothDeviceRepository.startScan()
        showToast(context, "Buscando dispositivos...")

        val availableDevices = bluetoothDeviceRepository.getAvailableDevices().filter { !connectedDevices.contains(it) }
        devicesAdapter.updateDevices(availableDevices)
    }

    /**
     * Observes the device list LiveData from the repository and updates the UI when new devices are found.
     */
    private fun observeDeviceList() {
        bluetoothDeviceRepository.getDevicesLiveData().observe(context as MainActivity) { devices ->
            val availableDevices = devices.filter { !connectedDevices.contains(it) }
            devicesAdapter.updateDevices(availableDevices)
        }
    }

    /**
     * Observes the connected devices and updates the UI when new devices are connected.
     */
    private fun observeConnectedDevices() {
        viewModel.deviceName.observe(context as MainActivity) { name ->
            name?.let {
                connectedDevicesAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Disconnects a Bluetooth device and updates the connected devices list.
     *
     * @param device The Bluetooth device to disconnect.
     */
    private fun disconnectDevice(device: BluetoothDevice) {
        bluetoothDeviceRepository.disconnectGatt(device)
        connectedDevices.remove(device)
        connectedDevicesAdapter.notifyDataSetChanged()
    }

    /**
     * Cleans up resources when the activity is destroyed, including stopping Bluetooth scans
     * and disconnecting all devices.
     */
    fun cleanup() {
        bluetoothDeviceRepository.stopScan()
        bluetoothDeviceRepository.disconnectAll()
    }
}
