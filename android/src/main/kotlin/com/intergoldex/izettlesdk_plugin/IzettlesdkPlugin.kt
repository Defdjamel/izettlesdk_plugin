package com.intergoldex.izettlesdk_plugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.*



import com.izettle.android.auth.IZettleAuth
import com.izettle.android.commons.ext.state.toLiveData
import com.izettle.android.commons.state.StateObserver
import com.izettle.payments.android.payment.TransactionReference
import com.izettle.payments.android.sdk.IZettleSDK
import com.izettle.payments.android.sdk.IZettleSDK.Instance.init
import com.izettle.payments.android.sdk.User
import com.izettle.payments.android.sdk.IZettleSDK.Instance.user
import com.izettle.payments.android.ui.SdkLifecycle
import com.izettle.payments.android.ui.payment.CardPaymentActivity
import com.izettle.payments.android.ui.payment.CardPaymentResult
import com.izettle.payments.android.ui.readers.CardReadersActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.util.*


/** IzettlesdkPlugin */
class IzettlesdkPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private val TAG = "IzettleSDKPlugin"
  private lateinit var context: Context
  private lateinit var activity: Activity
  private lateinit var currentOperation: IzettlePluginResponse
  private var currentUser : IzettleUser? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(TAG, "onAttachedToEngine")
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "izettlesdk_plugin")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext

  }


  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.d(TAG, "onAttachedToActivity")
    activity = binding.activity
    binding.addActivityResultListener(this)


  }


  override fun onDetachedFromActivityForConfigChanges() {
    Log.d(TAG, "onDetachedFromActivityForConfigChanges")

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    Log.d(TAG, "onReattachedToActivityForConfigChanges")
  }

  override fun onDetachedFromActivity() {
    Log.d(TAG, "onDetachedFromActivity")
    IZettleSDK.user.state.removeObserver(authObserver)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }



  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    Log.d(TAG, call.method)
    currentOperation = IzettlePluginResponse(result)
    currentOperation.methodName = call.method

    when (call.method) {
      "initSDK" -> initSDK(call.argument<String>("client_id")!!, call.argument<String>("redirect_url")!!, result).flutterResult()
      "login" -> doLogin(result).flutterResult()
      "logout" -> logout(result).flutterResult()
      "isLoggedIn" -> isLoggedIn(result).flutterResult()
      "checkout" -> checkout(call.argument<Map<String, String>>("payment")!!, call.argument<Map<String, String>>("info"))

      "openSettings" -> openSettingsSDK(result).flutterResult()
      "getPlatformVersion" -> result.success("Andromerde ${android.os.Build.VERSION.RELEASE}")


      else -> result.notImplemented()
    }

  }


  private fun doLogin(@NonNull methodResult: Result): IzettlePluginResponse {
    IZettleSDK.user.login(activity)

  var response = IzettlePluginResponse(methodResult)
    response.message =  mutableMapOf("doLogin message" to true)
    response.status = true
    response.methodName = "doLogin"
    return response
  }
  private fun logout(@NonNull methodResult: Result): IzettlePluginResponse{
    IZettleSDK.user.logout()

    var response = IzettlePluginResponse(methodResult)
    response.message =  mutableMapOf("logout message" to true)
    response.status = true
    response.methodName = "logout"
    return response

  }

  private fun isLoggedIn( @NonNull methodResult: Result): IzettlePluginResponse{
    Log.d(TAG,  "isLoggedIn")


    var response = IzettlePluginResponse(methodResult)

    response.message = mutableMapOf("isLoggedIn" to false,"user" to  currentUser?.toMap() )
    response.status = currentUser != null
    response.methodName = "isLoggedIn"
    return response


  }


  private val authObserver = object : StateObserver<User.AuthState> {
    override fun onNext(state: User.AuthState) {
      when (state) {
        is User.AuthState.LoggedIn -> {
          Log.d(TAG,"logged in !")
           currentUser = IzettleUser(state.info)

        }// User authorized
        is User.AuthState.LoggedOut -> {
          Log.d(TAG,"logged Out !")
          currentUser = null
        } // There is no authorized use
      }
    }
  }

  private fun initSDK(@NonNull clientId: String, @NonNull redirect_url: String, @NonNull methodResult: Result): IzettlePluginResponse{

    Log.d(TAG, clientId)
    Log.d(TAG, redirect_url)
   init(context, clientId, redirect_url)
    ProcessLifecycleOwner.get().lifecycle.addObserver(SdkLifecycle(IZettleSDK))
    user.state.addObserver(authObserver)

  var response = IzettlePluginResponse(methodResult)
    response.message =  mutableMapOf("initSDK message" to true)
    response.status = true
    response.methodName = "initSDK"
  return response

  }

  private fun openSettingsSDK(@NonNull methodResult: Result): IzettlePluginResponse{
    val intent = CardReadersActivity.newIntent(this.context)

    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent)

    var response = IzettlePluginResponse(methodResult)
    response.message =  mutableMapOf("openSettingsSDK message" to true)
    response.status = true
    response.methodName = "openSettingsSDK"
    return response

  }
  private fun checkout(@NonNull args: Map<String, Any?>, @Nullable info: Map<String, String>?) {

    val internalTraceId = args["foreignTransactionId"] as String;


    val reference = TransactionReference.Builder(internalTraceId)

            .apply {
              if (info != null ) {
                for ((k, v) in info) {
                  this.put(k,v)
                }
              }
            }
            .put("testkey","valtest")
            .build()

    Log.d(TAG, reference.toString())


    //add extra info
   val amount = args["total"] as Double;


    val intent = CardPaymentActivity.IntentBuilder(context)
            // MANDATORY: Transaction amount in account currency
            .amount((amount*100).toLong())
            // MANDATORY, Reference object created in previous step
            .reference(reference)
            // MANDATORY, you can enable login prompt in the payment flow if user is not yet logged-in
            .enableLogin(true)
            // OPTIONAL, you can enable tipping (disabled by default)
            // This option will only work for markets with tipping support
            .enableTipping(false)
            // OPTIONAL, you can enable installments (enabled by default)
            // This option will only work for markets with installments support
            .enableInstalments(false)
            .build()

// Start activity with the intent
   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent)




  }
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    Log.d(TAG, "onActivityResult - RequestCode: $requestCode - Result Code: $resultCode")


    if (requestCode == REQUEST_CODE_PAYMENT && data != null) {
      val result: CardPaymentResult? = data.getParcelableExtra(CardPaymentActivity.RESULT_EXTRA_PAYLOAD)
      var paymentValid = false
      if (result is CardPaymentResult.Completed) {
        paymentValid = true
        Toast.makeText(context, "Payment completed", Toast.LENGTH_SHORT).show()
      } else if (result is CardPaymentResult.Canceled) {
        Toast.makeText(context, "Payment canceled", Toast.LENGTH_SHORT).show()
      } else if (result is CardPaymentResult.Failed) {
        Toast.makeText(context, "Payment failed ", Toast.LENGTH_SHORT).show()
      }
      currentOperation.message = mutableMapOf(
              "success" to paymentValid
      )
      currentOperation.flutterResult()
      return true
    }
    return false
  }



  companion object {
    private const val REQUEST_CODE_PAYMENT = 1001
    private const val REQUEST_CODE_REFUND = 1002
  }
}





class IzettlePluginResponse(@NonNull var methodResult: Result) {
  var status: Boolean = false
  lateinit var message: MutableMap<String, Any?>
  lateinit var methodName: String
  fun toMap(): Map<String, Any?> {
    return mapOf("status" to status, "message" to message, "methodName" to methodName)
  }

  fun flutterResult() {
    methodResult.success(this.toMap())
  }
}


class IzettleUser(var info : com.izettle.payments.android.sdk.User.Info){
   var name: String?
    var userId: String?

  init{
    name = info.publicName.toString()
    userId = info.userId.toString()
  }
  fun toMap(): Map<String, Any?> {
    return mapOf("name" to name,"userId" to userId)
  }

}