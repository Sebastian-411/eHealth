package com.example.arduinonano.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.arduinonano.R

class ConnectedDeviceAdapter(
    context: Context,
    connectedDevices: MutableList<BluetoothDevice>,
    private val onDisconnectClicked: (BluetoothDevice) -> Unit
) : ArrayAdapter<BluetoothDevice>(context, 0, connectedDevices) {

    @SuppressLint("MissingPermission")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.connected_device_item, parent, false)

        val deviceName = view.findViewById<TextView>(R.id.connectedDeviceName)
        val disconnectButton = view.findViewById<Button>(R.id.disconnectButton)

        deviceName.text = device?.name ?: "Unknown Device"
        disconnectButton.setOnClickListener {
            if (device != null) {
                onDisconnectClicked(device)
            }
        }

        return view
    }
}
