package com.example.arduinonano.ui

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
    private lateinit var startDataCaptureButton: Button
    private lateinit var stopDataCaptureButton: Button
    private lateinit var patientNameEditText: EditText
    private lateinit var devicesAdapter: BluetoothDeviceListAdapter
    private lateinit var connectedDevicesAdapter: ConnectedDeviceAdapter

    private val connectedDevices: MutableList<BluetoothDevice> = mutableListOf()

    /**
     * Initializes the UI components and sets up listeners for button clicks and list updates.
     */
    fun setupUI() {
        bindViews()
        setupAdapters()
        setupListeners()
        observeViewModel()
        observeDevicesLiveData()
    }

    private fun bindViews() {
        connectNewDeviceButton = (context as MainActivity).findViewById(R.id.connectNewDeviceButton)
        startDataCaptureButton = context.findViewById(R.id.startDataCaptureButton)
        stopDataCaptureButton = context.findViewById(R.id.stopDataCaptureButton)
        patientNameEditText = context.findViewById(R.id.patientNameEditText)
        connectedDevicesListView = context.findViewById(R.id.connectedDevicesListView)
        deviceListView = context.findViewById(R.id.deviceListView)
    }

    private fun setupAdapters() {
        devicesAdapter = BluetoothDeviceListAdapter(context, mutableListOf())
        connectedDevicesAdapter = ConnectedDeviceAdapter(context, connectedDevices, this::disconnectDevice)

        deviceListView.adapter = devicesAdapter
        connectedDevicesListView.adapter = connectedDevicesAdapter
    }

    private fun setupListeners() {
        connectNewDeviceButton.setOnClickListener { startBluetoothScan() }

        setupDeviceListListener()
        setupStartDataCaptureButton()
        setupStopDataCaptureButton()
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
            updateStartDataCaptureButtonState()
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

    private fun setupStartDataCaptureButton() {
        startDataCaptureButton.setOnClickListener {
            val patientName = patientNameEditText.text.toString()
            if (isValidPatientName(patientName)) {
                startDataCaptureForConnectedDevices()
            } else {
                showToast(context, "Por favor ingresa un nombre válido para el paciente.")
            }
        }
        patientNameEditText.addTextChangedListener { updateStartDataCaptureButtonState() }
    }

    private fun setupStopDataCaptureButton() {
        stopDataCaptureButton.setOnClickListener {
            bluetoothDeviceRepository.stopCapture()
            viewModel.updateSendingDataStatus(false)
            showToast(context, "Toma de datos detenida.")
        }
    }

    private fun observeViewModel() {
        viewModel.sendingData.observe(context as MainActivity) { updateStartDataCaptureButtonState() }
    }

    private fun observeDevicesLiveData() {
        bluetoothDeviceRepository.getDevicesLiveData().observe(context as MainActivity) { devices ->
            val availableDevices = devices.filter { !connectedDevices.contains(it) }
            devicesAdapter.updateDevices(availableDevices)
        }
    }

    private fun updateStartDataCaptureButtonState() {
        val isCapturing = viewModel.sendingData.value ?: false
        startDataCaptureButton.isEnabled = !isCapturing && connectedDevices.isNotEmpty() && isValidPatientName(patientNameEditText.text.toString())
        startDataCaptureButton.isVisible = !isCapturing
        stopDataCaptureButton.isEnabled = isCapturing
        stopDataCaptureButton.isVisible = isCapturing
        patientNameEditText.isEnabled = !isCapturing
    }

    private fun isValidPatientName(name: String): Boolean {
        return name.isNotBlank()
    }

    private fun startDataCaptureForConnectedDevices() {
        bluetoothDeviceRepository.startCapture()
        viewModel.updateSendingDataStatus(true)
        showToast(context, "Toma de datos iniciada.")
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