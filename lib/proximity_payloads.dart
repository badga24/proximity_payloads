library;
import 'proximity_payloads_platform_interface.dart';
import 'dart:async';
import 'package:flutter/services.dart';

class ProximityPayloads {

  static const MethodChannel _methodChannel =
  MethodChannel('proximity_payloads');

  static const EventChannel _eventChannel =
  EventChannel('proximity_payloads_events');

  static final StreamController<Map<String, dynamic>> _payloadController =
  StreamController.broadcast();

  static Stream<Map<String, dynamic>> get onPayloadReceived =>
      _payloadController.stream;

  static bool _isListening = false;


  Future<String?> getPlatformVersion() {
    return ProximityPayloadsPlatform.instance.getPlatformVersion();
  }

  /// Lance l’échange de payloads via BLE/NFC
  static Future<void> startExchange({required Map<String, String> payload}) async {
    _startListening(); // démarre l’écoute des événements côté Dart
    await _methodChannel.invokeMethod('start', payload);
  }

  /// Envoie un nouveau payload mis à jour à tout moment
  static Future<void> sendUpdatedPayload(Map<String, String> payload) async {
    await _methodChannel.invokeMethod('sendUpdatedPayload', payload);
  }

  /// Arrête l’échange
  static Future<void> stop() async {
    await _methodChannel.invokeMethod('stop');
  }

  /// Écoute les événements entrants du EventChannel
  static void _startListening() {
    if (_isListening) return;
    _isListening = true;

    _eventChannel.receiveBroadcastStream().listen((event) {
      if (event is Map) {
        _payloadController.add(Map<String, dynamic>.from(event));
      }
    }, onError: (e) {
      print('Erreur lors de la réception d’un payload: $e');
    });
  }
}
