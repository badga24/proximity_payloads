import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'proximity_payloads_platform_interface.dart';

/// An implementation of [ProximityPayloadsPlatform] that uses method channels.
class MethodChannelProximityPayloads extends ProximityPayloadsPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('proximity_payloads');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
