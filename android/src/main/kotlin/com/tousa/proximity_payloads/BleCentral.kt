package com.tousa.proximity_payloads

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import android.os.Handler
import android.os.Looper

class BleCentral(private val context: Context, private val eventSink: EventChannel.EventSink?) {

    private val SERVICE_UUID = UUID.fromString("0000aaaa-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("0000aaab-0000-1000-8000-00805f9b34fb")

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    fun startScanning() {
        val filters = listOf(ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build())

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(filters, settings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = try {
                device.name ?: "Inconnu"
            } catch (e: SecurityException) {
                Log.w("BLE", "Impossible d'accéder à device.name: ${e.message}")
                "Inconnu"
            }

            Log.d("BLE", "Found device: $deviceName")
            connectToDevice(device)
            scanner.stopScan(this) // auto-stop after first found
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun stopScanning() {
        scanner?.stopScan(scanCallback)
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("BLE", "Stopped scanning and closed GATT client")
    }


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT")
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic != null) {
                gatt.readCharacteristic(characteristic)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val value = characteristic.value
                val raw = String(value, Charset.forName("UTF-8"))
                val cleanJson = raw.trimEnd { it == '\u0000' || it.isWhitespace() }.takeWhile { it != '\u0000' } // coupe les fins nulles si besoin

                try {
                    val jsonObj = JSONObject(cleanJson)
                    Log.d("BLE", "Payload received: $jsonObj")

                    val map = jsonObj.keys().asSequence()
                        .associateWith { key -> jsonObj.getString(key) }


                    Handler(Looper.getMainLooper()).post {
                        eventSink?.success(map)
                    }
                } catch (e: Exception) {
                    Log.e("BLE", "Failed to parse JSON: $e")
                }
            }
        }

    }
}
