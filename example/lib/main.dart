import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const ProximityDemoApp());
}

class ProximityDemoApp extends StatelessWidget {
  const ProximityDemoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: ProximityHomePage(),
    );
  }
}

class ProximityHomePage extends StatefulWidget {
  const ProximityHomePage({super.key});

  @override
  State<ProximityHomePage> createState() => _ProximityHomePageState();
}

class _ProximityHomePageState extends State<ProximityHomePage> {
  static const MethodChannel _channel = MethodChannel('proximity_payloads');
  static const EventChannel _events = EventChannel('proximity_payloads_events');

  final List<Map<String, dynamic>> _receivedPayloads = [];

  @override
  void initState() {
    super.initState();
    _askPermissions().then((granted) {
      if (granted) {
        _listenToIncomingPayloads();
        _startExchange();
      } else {
        print("Permissions refusées");
      }
    });
  }

  Future<bool> _askPermissions() async {
    final statuses = await [
      Permission.bluetooth,
      Permission.bluetoothConnect,
      Permission.bluetoothAdvertise,
      Permission.bluetoothScan,
      Permission.locationWhenInUse,
    ].request();

    return statuses.values.every((status) => status.isGranted);
  }

  Future<void> _startExchange() async {
    const myPayload = {
      "name": "Alice",
      "phone": "+22912345678",
    };
    await _channel.invokeMethod('start', myPayload);
  }

  void _listenToIncomingPayloads() {
    print("Outside events");
    _events.receiveBroadcastStream().listen((event) {
      print("event");
      print(event);
      if (!mounted) return;
      setState(() {
        _receivedPayloads.add(Map<String, dynamic>.from(event));
      });
    }, onError: (e) {
      print("Erreur lors de la réception : $e");
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Proximity Payloads Demo')),
      body: ListView.builder(
        itemCount: _receivedPayloads.length,
        itemBuilder: (context, index) {
          final payload = _receivedPayloads[index];
          return ListTile(
            leading: const Icon(Icons.bluetooth),
            title: Text(payload['name'] ?? 'Inconnu'),
            subtitle: Text(payload['phone'] ?? ''),
          );
        },
      ),
    );
  }
}
