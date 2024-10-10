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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arduinonano.gatt.GattHandler

/**
 * MainActivity manages the user interface for scanning and connecting to Bluetooth devices.
 * It handles Bluetooth permissions, scanning, and displaying discovered devices.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesAdapter: ArrayAdapter<String>
    private val devicesList = mutableListOf<BluetoothDevice>()
    private var gattHandler: GattHandler? = null

    private lateinit var scanButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var deviceListView: ListView

    /**
     * Initializes the activity, UI elements, and Bluetooth components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        // Initialize UI elements
        scanButton = findViewById(R.id.scanButton)
        disconnectButton = findViewById(R.id.disconnectButton)
        deviceListView = findViewById(R.id.deviceListView)

        // Initialize Bluetooth components
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Set up the ArrayAdapter to display discovered devices
        devicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        deviceListView.adapter = devicesAdapter

        // Set up scan button click listener
        scanButton.setOnClickListener { startBluetoothScan() }

        // Set up disconnect button click listener
        disconnectButton.setOnClickListener { gattHandler?.disconnectGatt() }

        // Register receiver to handle discovered Bluetooth devices
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        // Handle device selection from the list
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = devicesList[position]
            gattHandler = GattHandler(this)
            gattHandler?.connectGatt(device)
        }
    }

    /**
     * Requests necessary Bluetooth permissions using the PermissionHandler.
     */
    private fun requestPermissions() {
        val permissionHandler = PermissionHandler(this) { granted ->
            if (!granted) {
                showToast("Bluetooth permissions are required.")
            }
        }
        permissionHandler.checkPermissions()
    }

    /**
     * Cleans up resources, disconnects GATT, and unregisters the broadcast receiver.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        gattHandler?.disconnectGatt()
    }

    /**
     * Starts Bluetooth scanning and updates the UI with discovered devices.
     */
    @SuppressLint("MissingPermission")
    private fun startBluetoothScan() {
        devicesAdapter.clear()
        devicesList.clear()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        showToast("Scanning for devices...")
    }

    /**
     * BroadcastReceiver to handle discovered Bluetooth devices during the scan.
     */
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

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to be displayed.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
