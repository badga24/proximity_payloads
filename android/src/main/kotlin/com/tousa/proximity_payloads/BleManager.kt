package com.tousa.proximity_payloads

import android.content.Context
import io.flutter.plugin.common.EventChannel

object BleManager {
    private lateinit var peripheral: BlePeripheral
    private lateinit var central: BleCentral

    fun startAdvertising(context: Context, payload: Map<String, String>) {
        peripheral = BlePeripheral(context, payload)
        peripheral.startAdvertising()
    }

    fun startScanning(context: Context, eventSink: EventChannel.EventSink?) {
        central = BleCentral(context, eventSink)
        central.startScanning()
    }

    fun sendPayload(payload: Map<String, String>) {
        peripheral.send(payload)
    }
}