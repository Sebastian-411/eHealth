package com.example.arduinonano.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.arduinonano.R
import android.bluetooth.BluetoothDevice

/**
 * Custom adapter for displaying Bluetooth devices in a ListView.
 *
 * @param context The context where the adapter is used.
 * @param devices List of Bluetooth devices to display.
 */
class BluetoothDeviceListAdapter(
    context: Context,
    private val devices: MutableList<BluetoothDevice>
) : ArrayAdapter<BluetoothDevice>(context, 0, devices) {

    @SuppressLint("MissingPermission")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.device_list_item, parent, false)

        val deviceName = view.findViewById<TextView>(R.id.deviceName)
        val deviceAddress = view.findViewById<TextView>(R.id.deviceAddress)

        deviceName.text = device?.name ?: "Unknown Device"
        deviceAddress.text = device?.address

        return view
    }

    /**
     * Updates the list of Bluetooth devices and refreshes the adapter.
     *
     * @param newDevices New list of Bluetooth devices to display.
     */
    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }
}
