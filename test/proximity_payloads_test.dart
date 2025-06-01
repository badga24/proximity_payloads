import 'package:flutter_test/flutter_test.dart';
import 'package:proximity_payloads/proximity_payloads.dart';
import 'package:proximity_payloads/proximity_payloads_platform_interface.dart';
import 'package:proximity_payloads/proximity_payloads_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockProximityPayloadsPlatform
    with MockPlatformInterfaceMixin
    implements ProximityPayloadsPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final ProximityPayloadsPlatform initialPlatform = ProximityPayloadsPlatform.instance;

  test('$MethodChannelProximityPayloads is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelProximityPayloads>());
  });

  test('getPlatformVersion', () async {
    ProximityPayloads proximityPayloadsPlugin = ProximityPayloads();
    MockProximityPayloadsPlatform fakePlatform = MockProximityPayloadsPlatform();
    ProximityPayloadsPlatform.instance = fakePlatform;

    expect(await proximityPayloadsPlugin.getPlatformVersion(), '42');
  });
}
