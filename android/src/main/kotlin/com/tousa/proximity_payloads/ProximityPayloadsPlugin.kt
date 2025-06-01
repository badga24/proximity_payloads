package com.tousa.proximity_payloads

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.util.Log

/** ProximityPayloadsPlugin */
class ProximityPayloadsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var context: Context
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel

  private var eventSink: EventChannel.EventSink? = null
  private var proximityPayloadManager: ProximityPayloadManager? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext

    methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "proximity_payloads")
    methodChannel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "proximity_payloads_events")
    eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.d("BLE", "EventChannel listener registered: $events")
        eventSink = events
      }

      override fun onCancel(arguments: Any?) {
        eventSink = null
      }
    })
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "start" -> {
        val args = call.arguments as? Map<*, *>
        val payload = args?.mapNotNull {
          val key = it.key?.toString()
          val value = it.value?.toString()
          if (key != null && value != null) key to value else null
        }?.toMap() ?: emptyMap()

        proximityPayloadManager = ProximityPayloadManager(context, payload, eventSink)
        proximityPayloadManager?.start()

        result.success(null)
      }
      "sendUpdatedPayload" -> {
        val args = call.arguments as? Map<*, *>
        val payload = args?.mapNotNull {
          val key = it.key?.toString()
          val value = it.value?.toString()
          if (key != null && value != null) key to value else null
        }?.toMap() ?: emptyMap()

        proximityPayloadManager?.sendUpdatedPayload(payload)
        result.success(null)
      }
      "stop" -> {
        proximityPayloadManager?.stop()
        result.success(null)
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }

  // Required for ActivityAware (permissions, etc.)
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    // You can use this if you need to request permissions
  }

  override fun onDetachedFromActivity() {}
  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
  override fun onDetachedFromActivityForConfigChanges() {}
}
