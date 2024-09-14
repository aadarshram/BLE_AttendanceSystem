// Module for scanning Bluetooth LE broadcast signals
// Can filter based on Service UUID, Device Name, MAC Address, Service Data, Manufacturer Specific
// Data, Advertising Data Type, and corresponding Data.

// Import necessary libraries
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class ScanSignal(private val context: Context) {

    // Initialize Bluetooth services
    private val bluetoothManager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
    // Adapter to handle device list
    private val leDeviceListAdapter: LeDeviceListAdapter? = LeDeviceListAdapter()
    // Handle timing
    private var scanning = false
    private val handler = Handler()
    private val scanPeriod: Long = 10000 // 10 seconds

    private val scanCallback =
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    processScanResult(result)
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    super.onBatchScanResults(results)
                    results.forEach { result -> processScanResult(result) }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.e("ScanFailed", "Error code: $errorCode")
                }
            }

    // Process scan result
    private fun processScanResult(result: ScanResult) {
        val scanRecord = result.scanRecord
        val serviceUuids = scanRecord?.serviceUuids
        val device = result.device
        val rssi = result.rssi

        // Log the data received
        serviceUuids?.forEach { uuid ->
            Log.d(
                    "ScanResult",
                    "Device name: ${device.name ?: "Unknown Device"}, Address: ${device.address}, RSSI: $rssi, Service UUID: ${uuid.uuid}"
            )
        }

        leDeviceListAdapter?.apply {
            addDevice(device)
            notifyDataSetChanged()
        }
    }

    // Start scan
    fun startScan() {
        if (bluetoothLeScanner != null) {
            val serviceUuid = ParcelUuid(UUID.fromString("<UUID-here>")) // Example UUID
            val scanFilter = ScanFilter.Builder().setServiceUuid(serviceUuid).build()
            val scanFilters = listOf(scanFilter)
            val scanSettings =
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
        } else {
            Log.e("ScanError", "BluetoothLeScanner is not available")
        }
    }

    // Stop scan
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    // Scan management
    private fun scanLeDevice() {
        if (!scanning) {
            handler.postDelayed(
                    {
                        scanning = false
                        stopScan()
                    },
                    scanPeriod
            )

            scanning = true
            startScan()
        } else {
            scanning = false
            stopScan()
        }
    }
}
