package com.example.arduinonano

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.arduinonano.ui.MainActivityUIController
import com.example.arduinonano.bluetooth.BluetoothDeviceRepository
import com.example.arduinonano.utils.PermissionHandler
import com.example.arduinonano.utils.UIUtils.showToast
import com.example.arduinonano.viewmodel.BluetoothDeviceViewModel

/**
 * Main activity for the application which manages the Bluetooth scanning process
 * and delegates UI responsibilities to the MainActivityUIController.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: BluetoothDeviceViewModel by viewModels()
    private lateinit var uiController: MainActivityUIController
    private lateinit var bluetoothDeviceRepository: BluetoothDeviceRepository

    /**
     * Called when the activity is created. Sets up the UI controller, requests necessary permissions,
     * and starts observing the ViewModel for connection updates.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        bluetoothDeviceRepository = BluetoothDeviceRepository(this, viewModel)
        uiController = MainActivityUIController(this, viewModel, bluetoothDeviceRepository)
        uiController.setupUI()

        observeViewModel()
    }

    /**
     * Observes the BluetoothDeviceViewModel for changes in connection state and device name.
     */
    private fun observeViewModel() {
        viewModel.isConnected.observe(this) { connected ->
            uiController.updateConnectionState(connected)
        }

        viewModel.deviceName.observe(this) { name ->
            uiController.updateDeviceName(name)
        }
    }

    /**
     * Requests Bluetooth permissions from the user. If permissions are not granted, shows a toast message.
     */
    private fun requestPermissions() {
        val permissionHandler = PermissionHandler(this) { granted ->
            if (!granted) {
                showToast(this, "Bluetooth permissions are required.")
            }
        }
        permissionHandler.checkPermissions()
    }

    /**
     * Cleans up resources when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        uiController.cleanup()
    }
}
