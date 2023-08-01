package com.example.native_channel_app

// Flutter Imports
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

// Android Imports
import android.content.*
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.flutter.Log
import io.flutter.plugin.common.EventChannel

class MainActivity: FlutterActivity() {
    private val TOAST_CHANNEL = "com.raywayday/toast"
    private val BATTERY_CHANNEL = "com.raywayday/battery"
    private val EVENT_CHANNEL = "com.raywayday/charging"

    private  lateinit var channel: MethodChannel
    private  lateinit var toastChannel: MethodChannel
    private lateinit var eventChannel: EventChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        toastChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, TOAST_CHANNEL)

        toastChannel.setMethodCallHandler{ call, result ->
            if( call.method == "showToast" ) {
                Toast.makeText(this, "Hello from Android", Toast.LENGTH_LONG).show()
            } else {
                result.notImplemented()
            }
        }
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)

        eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
        eventChannel.setStreamHandler(MyStreamHandler(context))

        // Receive data from Flutter
        channel.setMethodCallHandler{ call, result ->
            if ( call.method == "getBatteryLevel") {
                val arguments = call.arguments as Map<String, String>
                val name = arguments["name"]
                val batteryLevel = getBatteryLevel()
                result.success("$name says: $batteryLevel%")
            } else {
                result.notImplemented()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Send data to Flutter
        Handler(Looper.getMainLooper()).postDelayed({
            val batteryLevel = getBatteryLevel()
            channel.invokeMethod("reportBatteryLevel", batteryLevel)
        }, 0)
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int = if( VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP ) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = ContextWrapper(applicationContext).registerReceiver(null, iFilter)
            intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100
        }
        return batteryLevel
    }

}

class MyStreamHandler(private val context: Context) : EventChannel.StreamHandler {
    private var receiver: BroadcastReceiver? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.w("onListen", "Adding  $events")
        if( events == null ) return

        receiver = initReceiver(events)
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun initReceiver(events: EventChannel.EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                Log.w("onReceive", "$batteryStatus")
                when (batteryStatus) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> {
                        events.success("Battery is charging")
                    }
                    BatteryManager.BATTERY_STATUS_FULL -> {
                        events.success("Battery is full")
                    }
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                        events.success("Battery is discharging")
                    }
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                        events.success("Battery is not charging")
                    }
                    BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                        events.success("Battery status unknown")
                    }
                }
            }
        }
    }

    override fun onCancel(arguments: Any?) {
        context.unregisterReceiver(receiver)
        receiver = null
    }

}
