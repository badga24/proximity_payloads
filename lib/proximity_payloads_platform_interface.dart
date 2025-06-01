import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'proximity_payloads_method_channel.dart';

abstract class ProximityPayloadsPlatform extends PlatformInterface {
  /// Constructs a ProximityPayloadsPlatform.
  ProximityPayloadsPlatform() : super(token: _token);

  static final Object _token = Object();

  static ProximityPayloadsPlatform _instance = MethodChannelProximityPayloads();

  /// The default instance of [ProximityPayloadsPlatform] to use.
  ///
  /// Defaults to [MethodChannelProximityPayloads].
  static ProximityPayloadsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ProximityPayloadsPlatform] when
  /// they register themselves.
  static set instance(ProximityPayloadsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
