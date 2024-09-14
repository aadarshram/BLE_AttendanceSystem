package com.example.prill

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d("Bluetooth", "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("Bluetooth", "Advertising failed with error code: $errorCode")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startAdvertisingButton = findViewById<Button>(R.id.startAdvertisingButton)
        startAdvertisingButton.setOnClickListener {
            startAdvertising()
        }
    }

    private fun startAdvertising() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "Bluetooth is not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                resultLauncher.launch(enableBtIntent)
            } else {
                // Request Bluetooth advertising permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE),
                    REQUEST_ENABLE_BT
                )
            }
        } else {
            // Bluetooth is already enabled, start BLE advertising
            startBleAdvertising(bluetoothAdapter)
        }
    }

    // Define the resultLauncher
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth is enabled, start BLE advertising
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            bluetoothAdapter?.let { startBleAdvertising(it) }
        } else {
            // Bluetooth is not enabled
            Toast.makeText(this, "Bluetooth needs to be enabled for this app to work.", Toast.LENGTH_LONG).show()
        }
    }

    private fun startBleAdvertising(bluetoothAdapter: BluetoothAdapter) {
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false) // Ensure it does not accept connections
            .build()

        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID.fromString("<UUID HERE>")))
            .setIncludeDeviceName(false)
            .build()

        try {
            bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
            Log.d("Bluetooth", "Advertising started successfully")
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Failed to start BLE advertising due to missing permissions", e)
            Toast.makeText(this, "Permission denied to start Bluetooth advertising.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            Log.d("Bluetooth", "Advertising stopped successfully")
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Failed to stop BLE advertising due to missing permissions", e)
            Toast.makeText(this, "Permission denied to stop Bluetooth advertising.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}
