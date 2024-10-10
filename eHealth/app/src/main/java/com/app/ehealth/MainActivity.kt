package com.app.ehealth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var bluetoothSocket: BluetoothSocket
    private var selectedDevice: BluetoothDevice? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID estándar SPP
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private val receivedData = mutableListOf<String>()

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BluetoothAppUI()
        }

        // Inicializa el BroadcastReceiver para escuchar dispositivos encontrados
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) Log.d("Bluetooth", "Dispositivo encontrado: ${device.name} - ${device.address}")
                }
            }
        }
    }

    @Composable
    fun BluetoothAppUI() {
        var receivedMessages by remember { mutableStateOf(listOf<String>()) }
        var selectedDeviceAddress by remember { mutableStateOf("") } // Dirección MAC del dispositivo seleccionado
        var isConnected by remember { mutableStateOf(false) } // Estado de conexión

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo para ingresar la dirección MAC
            TextField(
                value = selectedDeviceAddress,
                onValueChange = { newAddress -> selectedDeviceAddress = newAddress.toString() }, // Usar un nombre de variable
                label = { Text("Ingrese la dirección MAC") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            // Botón para conectar al dispositivo
            Button(onClick = { connectBluetooth(selectedDeviceAddress) }) {
                Text("Conectar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { receivedMessages = receiveData() }) {
                Text("Recibir Datos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { saveDataAsCsv(receivedMessages) }) {
                Text("Guardar CSV")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Datos recibidos:")
            receivedMessages.forEach { message ->
                Text(message)
            }
        }
    }

    private @Composable
    fun TextField(value: String, onValueChange: (Any?) -> Unit, label: @Composable () -> Unit, modifier: Modifier) {

    }

    private fun connectBluetooth(macAddress: String) {
        val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

        try {
            bluetoothSocket.connect() // Conecta al socket Bluetooth
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream
        } catch (e: Exception) {
            Log.e("BluetoothError", "Error de conexión: ${e.message}")
        }
    }

    private fun receiveData(): List<String> {
        val buffer = ByteArray(1024)
        var bytes: Int

        try {
            // Leer datos de Arduino
            bytes = inputStream.read(buffer)
            val incomingMessage = String(buffer, 0, bytes)

            Log.d("BluetoothData", "Mensaje recibido: $incomingMessage")

            // Guardar datos recibidos
            receivedData.add(incomingMessage)
        } catch (e: Exception) {
            Log.e("BluetoothError", "Error al leer datos: ${e.message}")
        }

        return receivedData
    }

    // Guardar datos en CSV
    private fun saveDataAsCsv(dataList: List<String>) {
        val fileName = "received_data.csv"
        val file = getExternalFilesDir(null)?.resolve(fileName)

        file?.printWriter()?.use { out ->
            dataList.forEach { data ->
                out.println(data)
            }
        }

        Log.d("CSV", "Datos guardados en $fileName")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegúrate de desregistrar el BroadcastReceiver
        unregisterReceiver(receiver)
    }

    // Previsualización de la UI en Jetpack Compose
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        BluetoothAppUI()
    }
}
