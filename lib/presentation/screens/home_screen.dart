import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:native_channel_app/presentation/screens/toast_screen.dart';

class HomeScreen extends StatefulWidget {
  
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const batteryChannel = MethodChannel('com.raywayday/battery');
  static const chargingChannel = EventChannel('com.raywayday/charging');

  late StreamSubscription _streamSubscription;
  String batteryLevel = 'Waiting...';
  String batteryLevelFromOnInit = 'initState...';
  String batteryLevelFromStream = 'Streaming...';

  @override
  void initState() {
    super.initState();
    onListenBattery();
    onStreamBattery();
  }

  @override
  void dispose() {
    super.dispose();
    _streamSubscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Native Channel App'),
        actions: [
          IconButton(
            onPressed: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => const ToastScreen()));
            },
            icon: const Icon(Icons.message)
          )
        ],
      ),
      body: PageView(
        scrollDirection: Axis.vertical,
        children: [
          _InvokeMethodView(
            batteryLevel: batteryLevel,
            getBatteryLevel: _getBatteryLevel
          ),
          _BatteryStatusView(
            batteryLevel: batteryLevelFromOnInit,
          ),
          _BatteryStatusView(
            batteryLevel: batteryLevelFromStream,
          )
        ],
      )
    );
  }

  Future<void> _getBatteryLevel() async {
    final Map<String, String> arguments = { 'name': 'César Arellano' };

    final String newBatteryLevel = await batteryChannel.invokeMethod('getBatteryLevel', arguments);
    setState(() {
      batteryLevel = newBatteryLevel;
    });
  }
  
  void onListenBattery() {
    batteryChannel.setMethodCallHandler((call) async {
      if( call.method == 'reportBatteryLevel' ) {
        final int batteryLevel = call.arguments;
        setState(() {
          batteryLevelFromOnInit = '$batteryLevel%';
        });
      }
    });
  }
  
  void onStreamBattery() {
    _streamSubscription = chargingChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        batteryLevelFromStream = '$event';
      });
    });
  }
}

class _InvokeMethodView extends StatelessWidget {
  const _InvokeMethodView({
    required this.batteryLevel,
    required this.getBatteryLevel
  });

  final String batteryLevel;
  final VoidCallback getBatteryLevel;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            batteryLevel,
            style: const TextStyle(
              fontSize: 28
            ),
          ),
          const SizedBox(height: 4),
          ElevatedButton(
            onPressed: getBatteryLevel,
            child: const Text('Get battery level')
          ),
        ],
      ),
    );
  }
}

class _BatteryStatusView extends StatelessWidget {
  const _BatteryStatusView({
    required this.batteryLevel,
  });

  final String batteryLevel;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        batteryLevel,
        style: const TextStyle(
          fontSize: 28
        ),
      ),
    );
  }
}