package com.example.arduinonano

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.arduinonano.gatt.GattHandler

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesAdapter: ArrayAdapter<String>
    private val devicesList = mutableListOf<BluetoothDevice>()
    private lateinit var gattHandler: GattHandler

    private lateinit var scanButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var deviceListView: ListView
    private lateinit var connectedDeviceName: TextView

    private val viewModel: BluetoothDeviceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        scanButton = findViewById(R.id.scanButton)
        disconnectButton = findViewById(R.id.disconnectButton)
        deviceListView = findViewById(R.id.deviceListView)
        connectedDeviceName = findViewById(R.id.connectedDeviceName)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        devicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        deviceListView.adapter = devicesAdapter

        scanButton.setOnClickListener { startBluetoothScan() }
        disconnectButton.setOnClickListener { gattHandler.disconnectGatt() }

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = devicesList[position]
            gattHandler = GattHandler(this, viewModel)
            gattHandler.connectGatt(device)
            stopBluetoothScan()
            updateUIForConnecting()
        }

        viewModel.isConnected.observe(this, Observer { connected ->
            if (connected) {
                showToast("Connected to device")
            } else {
                showToast("Disconnected from device")
                updateUIForDisconnected()
            }
        })

        viewModel.deviceName.observe(this, Observer { name ->
            connectedDeviceName.text = name ?: "No device connected"
        })

        viewModel.deviceAddress.observe(this, Observer { address ->
            // Update UI with device address if needed
        })
    }

    private fun requestPermissions() {
        val permissionHandler = PermissionHandler(this) { granted ->
            if (!granted) {
                showToast("Bluetooth permissions are required.")
            }
        }
        permissionHandler.checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        gattHandler.disconnectGatt()
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothScan() {
        stopBluetoothScan()
        bluetoothAdapter.startDiscovery()
        showToast("Scanning for devices...")
    }

    @SuppressLint("MissingPermission")
    private fun stopBluetoothScan() {
        devicesAdapter.clear()
        devicesList.clear()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    devicesList.add(it)
                    devicesAdapter.add("${it.name ?: "Unknown"} - ${it.address}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUIForConnecting() {
        devicesAdapter.clear()
        scanButton.isEnabled = false
        disconnectButton.isEnabled = true
    }

    private fun updateUIForDisconnected() {
        scanButton.isEnabled = true
        disconnectButton.isEnabled = false
        connectedDeviceName.text = "No device connected"
    }
}