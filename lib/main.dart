import 'package:flutter/material.dart';
import 'package:native_channel_app/presentation/screens/home_screen.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Native Channel App',
      home: const HomeScreen(),
      theme: ThemeData.dark().copyWith(
        useMaterial3: true
      ),
    );
  }
}