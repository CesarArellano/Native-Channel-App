import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ToastScreen extends StatelessWidget {
  static const toastChannel = MethodChannel('com.raywayday/toast');

  const ToastScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Toast Screen'),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: showToast,
          child: const Text('Show toast message')
        )
      ),
    );
  }

  Future<void> showToast() async {
    await toastChannel.invokeMethod('showToast');
  }
}