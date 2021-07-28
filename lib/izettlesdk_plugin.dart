import 'dart:async';
import 'dart:math';
import 'package:flutter/services.dart';

class IzettlesdkPlugin {
  static const MethodChannel _channel =
      const MethodChannel('izettlesdk_plugin');

  static bool _isInitialized = false;

  static void _throwIfNotInitialized() {
    if (!_isInitialized) {
      throw Exception(
          'Izettle SDK is not initialized. You should call IzettlesdkPlugin.initSDK(affiliateCliendID)');
    }
  }

  static Future<void> _throwIfNotLoggedIn() async {
    final isLogged = await isLoggedIn;
    if (!isLogged.status) {
      throw Exception('Not logged in. You must login before.');
    }
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<IzettlePluginResponse> get isLoggedIn async {
    _throwIfNotInitialized();
    var response = IzettlePluginResponse.fromMap(
        await _channel.invokeMethod('isLoggedIn'));

    return response;
  }

  // static Future<bool> isLogged() async {
  //   _throwIfNotInitialized();

  //   var response = IzettlePluginResponse.fromMap(
  //       await _channel.invokeMethod('isLoggedIn'));

  //   return response.status;
  // }

  static Future<IzettlePluginResponse> logout() async {
    _throwIfNotInitialized();

    var response =
        IzettlePluginResponse.fromMap(await _channel.invokeMethod('logout'));

    return response;
  }

  static Future<IzettlePluginResponse> initSDK(
      String clienID, String redirecturl) async {
    final dynamic response =
        IzettlePluginResponse.fromMap(await _channel.invokeMethod('initSDK', {
      "client_id": clienID,
      "redirect_url": redirecturl,
    }));

    if (response.status) {
      _isInitialized = true;
    }
    return response;
  }

  static Future<IzettlePluginResponse> login() async {
    _throwIfNotInitialized();
    return IzettlePluginResponse.fromMap(await _channel.invokeMethod('login'));
  }

  /// Sets up card terminal
  ///
  /// Login required
  static Future<IzettlePluginResponse> openSettings() async {
    _throwIfNotInitialized();
    //await _throwIfNotLoggedIn();

    return IzettlePluginResponse.fromMap(
        await _channel.invokeMethod('openSettings'));
  }

  static Future<IzettlePluginResponse> checkout(
      IzettlePaymentRequest paymentRequest) async {
    _throwIfNotInitialized();
    _throwIfNotLoggedIn();
    //await _throwIfNotLoggedIn();
    var rng = new Random();
    // var refUuid = rng.nextInt(100000000);
    // var payment = IzettlePayment(
    //   total: 1.2,
    //   foreignTransactionId: refUuid.toString(),
    //   tip: .0,
    // );

    // var paymentRequest = IzettlePaymentRequest(payment, info: {
    //   'AccountId': 'taxi0334',
    //   'From': 'Paris',
    //   'To': 'Berlin',
    // });

    return IzettlePluginResponse.fromMap(
        await _channel.invokeMethod('checkout', paymentRequest.toMap()));
  }
}

/// Response returned from native platform
class IzettlePluginResponse {
  IzettlePluginResponse({
    this.methodName,
    this.status = true,
    this.message,
  });
  String? methodName;
  bool status = true;
  Map<dynamic, dynamic>? message;

  IzettlePluginResponse.fromMap(Map<dynamic, dynamic> response) {
    methodName = response['methodName'];
    status = response['status'];
    message = response['message'];
  }

  String toString() {
    return 'Method: $methodName, status: $status, message: $message';
  }
}

/// Sumup payment request
class IzettlePaymentRequest {
  IzettlePaymentRequest(this.payment, {this.info});

  IzettlePayment payment;

  /// All the additional information associated with this payment
  Map<String, String>? info;

  Map<String, dynamic> toMap() => {
        'payment': payment.toMap(),
        'info': info,
      };
}

/// Sumup payment
class IzettlePayment {
  IzettlePayment({
    this.total = .0,
    this.tip = .0,
    this.foreignTransactionId = "",
  });

  double total, tip;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction
  String foreignTransactionId;

  Map<String, dynamic> toMap() => {
        'total': total,
        'tip': tip,
        'foreignTransactionId': foreignTransactionId,
      };
}
