package com.tousa.proximity_payloads

import android.content.Context
import android.util.Log
import io.flutter.plugin.common.EventChannel
import android.os.Handler
import android.os.Looper

class ProximityPayloadManager(
    private val context: Context,
    private val payloadToSend: Map<String, String>,
    private val eventSink: EventChannel.EventSink?
) {
    private var blePeripheral: BlePeripheral? = null
    private var bleCentral: BleCentral? = null

    fun start() {
        Log.d("BLE", "Payload reçu dans BlePeripheral: $payloadToSend")
        // 1. Lancer publicité BLE + GATT server
        blePeripheral = BlePeripheral(context, payloadToSend)
        blePeripheral?.startAdvertising()

        // 2. Lancer scan BLE pour découvrir et se connecter aux autres
        bleCentral = BleCentral(context, object : EventChannel.EventSink {
            override fun success(receivedPayload: Any?) {
                Log.d("Proximity", "Payload reçu: $receivedPayload")
                Handler(Looper.getMainLooper()).post {
                    eventSink?.success(receivedPayload)
                }
            }

            override fun error(code: String?, message: String?, details: Any?) {
                Handler(Looper.getMainLooper()).post {
                    eventSink?.error(code, message, details)
                }
            }

            override fun endOfStream() {
                Handler(Looper.getMainLooper()).post {
                    eventSink?.endOfStream()
                }
            }
        })
        bleCentral?.startScanning()
    }

    fun sendUpdatedPayload(newPayload: Map<String, String>) {
        blePeripheral?.send(newPayload)
    }

    fun stop() {
        // Optionnel : implémenter l’arrêt de scan/pub
    }
}
