package com.example.prillclient

// MainActivity.kt
import android.Manifest
import android.bluetooth.BluetoothAdapter

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID
import android.os.ParcelUuid
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private lateinit var resultTextView: TextView
    private var scanning = false
    // private val handler = Handler()
    private val scanPeriod: Long = 10000 // 10 seconds

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            stopScan()
            resultTextView.text = getString(R.string.Scan_succeeded)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            if (results.isNotEmpty()) {
                stopScan()
                resultTextView.text = getString(R.string.Scan_succeeded)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            stopScan()
            resultTextView.text = getString(R.string.Scan_failed)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton: Button = findViewById(R.id.scanButton)
        resultTextView = findViewById(R.id.resultTextView)

        // Initialize Bluetooth
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                resultTextView.text = getString(R.string.Scanning)
                startScan()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
            }
        }
    }

    private fun startScan() {
        bluetoothLeScanner?.let {
            val serviceUuid = ParcelUuid(UUID.fromString("your-uuid-here")) // Replace with your UUID
            val scanFilter = ScanFilter.Builder().setServiceUuid(serviceUuid).build()
            val scanFilters = listOf(scanFilter)
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            try {
                it.startScan(scanFilters, scanSettings, scanCallback)
                scanning = true

                // Stop scan after scanPeriod (10 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    if (scanning) {
                        stopScan()
                        resultTextView.text = getString(R.string.Scan_failed)
                    }
                }, scanPeriod)
            } catch (e: SecurityException) {
                Log.e("ScanError", "Permission denied or not granted: ${e.message}")
                resultTextView.text = getString(R.string.permission_denied)
            }
        } ?: run {
            Log.e("ScanError", "BluetoothLeScanner is not available")
            resultTextView.text = getString(R.string.Unavailable_scanner)
        }
    }

    private fun stopScan() {
        if (scanning) {
            try {
                bluetoothLeScanner?.stopScan(scanCallback)
                scanning = false
            } catch (e: SecurityException) {
                Log.e("StopScanError", "Permission denied or not granted: ${e.message}")
                resultTextView.text = getString(R.string.permission_denied_stop)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, start scan
            resultTextView.text = getString(R.string.Scanning)
            startScan()
        } else {
            resultTextView.text = getString(R.string.permission_denied)
        }
    }
}
