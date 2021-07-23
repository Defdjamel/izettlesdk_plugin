import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:izettlesdk_plugin/izettlesdk_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initSDK();
  }

  Future<void> initSDK() async {
    var init = await IzettlesdkPlugin.initSDK(
        "0d12f4bc-837c-41ca-bf7d-e32c11774f72", "monkiosk://izettle");
    print(init);
  }

  Future<void> openSettingSDK() async {
    var response = await IzettlesdkPlugin.openSettings();
    print(response);
  }

  Future<void> doLogin() async {
    var response = await IzettlesdkPlugin.login();
    print(response);
  }

  Future<void> checkOut() async {
    var response = await IzettlesdkPlugin.checkout();
    print(response);
  }

  Future<void> logout() async {
    var response = await IzettlesdkPlugin.logout();
    print(response);
  }

  Future<void> isLoggedIn() async {
    var response = await IzettlesdkPlugin.isLoggedIn;
    print(response);
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await IzettlesdkPlugin.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              TextButton(
                  onPressed: () {
                    initSDK();
                  },
                  child: Text("INIT SDK")),
              TextButton(
                  onPressed: () {
                    doLogin();
                  },
                  child: Text("Login")),
              TextButton(
                  onPressed: () {
                    openSettingSDK();
                  },
                  child: Text("openSettingSDK")),
              TextButton(
                  onPressed: () {
                    checkOut();
                  },
                  child: Text("checkOut")),
              TextButton(
                  onPressed: () {
                    isLoggedIn();
                  },
                  child: Text("IsLoggedIn")),
              TextButton(
                  onPressed: () {
                    logout();
                  },
                  child: Text("Logiut")),
              Text("Version is  $_platformVersion")
            ],
          ),
        ),
      ),
    );
  }
}
