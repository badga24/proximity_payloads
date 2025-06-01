import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:proximity_payloads/proximity_payloads_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelProximityPayloads platform = MethodChannelProximityPayloads();
  const MethodChannel channel = MethodChannel('proximity_payloads');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
