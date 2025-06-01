package com.tousa.proximity_payloads

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.charset.Charset
import java.util.*
import org.json.JSONObject

class BlePeripheral(private val context: Context, private val payload: Map<String, String>) {

    private val SERVICE_UUID = UUID.fromString("0000aaaa-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("0000aaab-0000-1000-8000-00805f9b34fb")

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

    private lateinit var gattServer: BluetoothGattServer
    private lateinit var characteristic: BluetoothGattCharacteristic

    fun startAdvertising() {
        // Init GATT service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServer.addService(service)

        // Start BLE Advertising
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            //.setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
        Log.d("BLE", "Advertising started")
    }

    fun send(data: Map<String, String>) {

        val json = JSONObject(data).toString()
        Log.d("BLE", "Setting JSON: $json")
        val value = json.toByteArray(Charset.forName("UTF-8"))
        Log.d("BLE", "Byte array: ${value.joinToString()}")
        characteristic.value = byteArrayOf() // clear
        characteristic.value = value

        gattServer.connectedDevices.forEach {
            gattServer.notifyCharacteristicChanged(it, characteristic, false)
        }
        Log.d("BLE", "Sending payload: $json to ${gattServer.connectedDevices.size} device(s)")

        characteristic.value = byteArrayOf()
    }

    fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        Log.d("BLE", "Stopped advertising and closed GATT server")
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d("BLE", "Connection state changed: $device - $newState")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val json = JSONObject(payload).toString()
                val value = json.toByteArray(Charset.forName("UTF-8"))

                // 👇 Envoie uniquement la partie demandée
                val slice = if (offset >= value.size) byteArrayOf() else value.copyOfRange(offset, value.size)

                Log.d("BLE", "Sending payload from offset $offset: ${String(slice)}")

                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, slice)
            }
        }

        // not used for now
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean, responseNeeded: Boolean,
            offset: Int, value: ByteArray
        ) {
            val received = String(value, Charset.forName("UTF-8"))
            Log.d("BLE", "Received from central: $received")
            if (responseNeeded) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("BLE", "Advertise success")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE", "Advertise failed: $errorCode")
        }
    }
}
