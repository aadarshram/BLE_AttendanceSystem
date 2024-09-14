// ScanSignal.kt
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

class ScanSignal(private val context: Context, private val resultCallback: (Boolean) -> Unit) {

    private val bluetoothManager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner

    private var scanning = false
    private val handler = Handler()
    private val scanPeriod: Long = 10000 // 10 seconds

    private val scanCallback =
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    stopScan()
                    resultCallback(true) // Successfully received a result
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    super.onBatchScanResults(results)
                    if (results.isNotEmpty()) {
                        stopScan()
                        resultCallback(true) // Successfully received a result
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    stopScan()
                    resultCallback(false) // Scan failed
                }
            }

    fun startScan() {
        if (bluetoothLeScanner != null) {
            val serviceUuid = ParcelUuid(UUID.fromString("<UUID-here>")) // Example UUID
            val scanFilter = ScanFilter.Builder().setServiceUuid(serviceUuid).build()
            val scanFilters = listOf(scanFilter)
            val scanSettings =
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
            scanning = true

            handler.postDelayed(
                    {
                        if (scanning) {
                            stopScan()
                            resultCallback(false) // No result found within the period
                        }
                    },
                    scanPeriod
            )
        } else {
            Log.e("ScanError", "BluetoothLeScanner is not available")
            resultCallback(false)
        }
    }

    private fun stopScan() {
        if (scanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
            scanning = false
        }
    }
}
