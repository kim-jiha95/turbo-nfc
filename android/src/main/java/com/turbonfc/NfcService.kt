package com.turbonfc

import android.app.Service
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.nfc.Tag

class NfcService : Service() {
    private val binder = LocalBinder()
    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null
    private var isReading = false
    private var onTagDiscoveredCallback: ((String) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): NfcService = this@NfcService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun isNfcEnabled(): Boolean {
        val enabled = nfcAdapter?.isEnabled == true
        return enabled
    }

    fun handleTag(tag: Tag) {
        try {
            val tagId = bytesToHexString(tag.id)
            currentTag = tag
            isReading = true
            onTagDiscoveredCallback?.invoke(tagId)
        } catch (e: Exception) {
            Log.e("NfcService", "Error handling tag: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setOnTagDiscoveredCallback(callback: (String) -> Unit) {
        onTagDiscoveredCallback = callback
    }

    private fun bytesToHexString(bytes: ByteArray?): String {
        if (bytes == null) return ""
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}