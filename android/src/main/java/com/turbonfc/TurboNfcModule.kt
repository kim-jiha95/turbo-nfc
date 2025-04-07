package com.turbonfc

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class TurboNfcModule(private val reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingPromise: Promise? = null
    private var isReading: Boolean = false
    private var nfcService: NfcService? = null
    private var infoPageTagId: String? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NfcService.LocalBinder
            nfcService = binder.getService()
            nfcService?.setOnTagDiscoveredCallback { tagId ->
                sendEvent("onTagDiscovered", Arguments.createMap().apply {
                    putBoolean("success", true)
                    putString("tagId", tagId)
                })
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            nfcService = null
        }
    }

    override fun getName(): String = "TurboNfc"

    override fun canOverrideExistingModule(): Boolean {
        return true
    }

    private fun initNfcAdapter() {
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(reactContext)
        }
    }

    @ReactMethod
    fun isSupported(promise: Promise) {
        initNfcAdapter()
        if (nfcAdapter == null) {
            promise.reject("nfc_not_supported", "NFC is not supported on this device")
        } else {
            promise.resolve(true)
        }
    }

    @ReactMethod
    fun isEnabled(promise: Promise) {
        try {
            initNfcAdapter()
            if (nfcAdapter == null) {
                promise.resolve(false)
                return
            }
            promise.resolve(nfcAdapter!!.isEnabled)
        } catch (ex: Exception) {
            Log.e("TurboNfcModule", "Error checking NFC enabled state: ${ex.message}")
            promise.resolve(false)
        }
    }

    @ReactMethod
    fun startTagReading(promise: Promise) {
        Log.d("TurboNfcModule", "startTagReading called")
        val activity = currentActivity
        if (activity == null) {
            promise.reject("activity_null", "Activity is null")
            return
        }

        try {
            initNfcAdapter()
            if (nfcAdapter == null) {
                promise.reject("nfc_not_available", "NFC adapter is not available")
                return
            }

            if (!nfcAdapter!!.isEnabled) {
                promise.reject("nfc_disabled", "NFC is disabled")
                return
            }

            pendingPromise = promise
            isReading = true

            val intent = Intent(activity, activity.javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            val pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            try {
                nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, null, null)
                
                val eventData = Arguments.createMap().apply {
                    putString("status", "active")
                }
                sendEvent("nfcStatusChange", eventData)
                
                val resultData = Arguments.createMap().apply {
                    putBoolean("success", true)
                    putString("message", "NFC scanning started")
                }
                promise.resolve(resultData)
            } catch (e: Exception) {
                Log.e("TurboNfcModule", "Error in enableForegroundDispatch: ${e.message}")
                promise.reject("nfc_error", "Failed to start NFC scanning: ${e.message}")
            }
        } catch (ex: Exception) {
            Log.e("TurboNfcModule", "Error in startTagReading: ${ex.message}", ex)
            promise.reject("nfc_error", "Failed to start NFC scanning: ${ex.message}")
        }
    }

    @ReactMethod
    fun stopTagReading(promise: Promise) {
        try {
            isReading = false
            val activity = currentActivity
            if (activity != null && nfcAdapter != null) {
                nfcAdapter?.disableForegroundDispatch(activity)
                val eventData = Arguments.createMap().apply {
                    putString("status", "Stopped NFC scanning")
                }
                sendEvent("nfcStatusChange", eventData)
            }
            pendingPromise = null
            promise.resolve(true)
        } catch (ex: Exception) {
            Log.e("TurboNfcModule", "Error in stopTagReading: ${ex.message}", ex)
            promise.reject("stop_error", "Failed to stop NFC scanning: ${ex.message}")
        }
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        Log.d("TurboNfcModule", "Sending event: $eventName with params: $params")
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent == null) return
        Log.d("TurboNfcModule", "Sending event: $intent with params: $intent")
        
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("TurboNfcModule", "-------tag")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag != null) {
            val tagId = bytesToHexString(tag.id)
            Log.d("TurboNfcModule", "Tag detected in onNewIntent: $tagId, $tag")
            
            val isInfoPageTag = tagId == infoPageTagId
            
            sendEvent("onTagDiscovered", Arguments.createMap().apply {
                putBoolean("success", true)
                putString("tagId", tagId)
                putBoolean("isInfoPageTag", isInfoPageTag)
            })
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        // Implementation required by ActivityEventListener
    }

    @ReactMethod
    fun setInfoPageTagId(tagId: String, promise: Promise) {
        infoPageTagId = tagId
        promise.resolve(true)
    }
}