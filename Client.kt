// This program is for the client side

// To scan for a BLE device
private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
private var scanning = false
private val handler = Hander()

// Need to stop after a while. Say, 15 seconds
private val SCAN_PERIOD: Long = 10000

private fun scanLeDevice() {
    if (!scanning) {
        handler.postDelayed({
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)

        }, SCAN_PERIOD)
        scanning = true
        bluetoothLeScanner.startScan(List<ScanFilter>, ScanSettings, leScanCallback) // Implement ScanFilter for restricting devices the scan looks for and ScanSettings for parameters about the scan
    } else {
        scanning = false
        bluetoothLeScanner.stopScan(leScanCallback)
    }
}

// Implement callback to return after scan
private val leDeviceListAdapter = leDeviceListAdapter()
// Device scan callback
private val leScanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        leDeviceListAdapter.addDevice(result.device)
        leDeviceListAdapter.notifyDataSetChanged()
    }
}
// Can only scan for either BLE or classic bluetooth at a time

// Connect to a GATT server

// connect to device
var bluetoothGatt: BluetoothGatt? = null
bluetoothGatt = device.connectGatt(this, false, gattCallback)

// Bound Service set up
class BluetoothLeService : Service() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }
}

// Bind to the service
class DeviceControlActivity : AppCompatActivity() {

    private var bluetoothService : BluetoothLeService? = null

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                // call functions on service to check connection and connect to devices
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gatt_services_characteristics)

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}

// Set up the BluetoothAdapter
private const val TAG = "BluetoothLeService"

class BluetoothLeService : Service() {

    private var bluetoothAdapter: BluetoothAdapter? = null

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false //show appropriate message for failure
        }
        return true
    }
}

// Finish 
class DeviceControlActivity : AppCompatActivity() {

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                // perform device connection
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }
}

// Connect service to device
fun connect(address: String): Boolean {
    bluetoothAdapter?.let { adapter ->
        try {
            val device = adapter.getRemoteDevice(address)
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Device not found with provided address.")
            return false
        }
    // connect to the GATT server on the device
    } ?: run {
        Log.w(TAG, "BluetoothAdapter not initialized")
        return false
    }
}

// Code to manage Service lifecycle.
private val serviceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(
    componentName: ComponentName,
    service: IBinder
    ) {
        bluetoothService = (service as LocalBinder).getService()
        bluetoothService?.let { bluetooth ->
            if (!bluetooth.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // perform device connection
            bluetooth.connect(deviceAddress)
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        bluetoothService = null
    }
}

// GATT callback
private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // successfully connected to the GATT Server
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // disconnected from the GATT Server
        }
    }
}

// Connect to GATT service
class BluetoothLeService : Service() {

    ...
    
        private var bluetoothGatt: BluetoothGatt? = null
    
        ...
    
        fun connect(address: String): Boolean {
            bluetoothAdapter?.let { adapter ->
                try {
                    val device = adapter.getRemoteDevice(address)
                    // connect to the GATT server on the device
                    bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                    return true
                } catch (exception: IllegalArgumentException) {
                    Log.w(TAG, "Device not found with provided address.  Unable to connect.")
                    return false
                }
            } ?: run {
                Log.w(TAG, "BluetoothAdapter not initialized")
                return false
            }
        }
    }

// broadcast update
private fun broadcastUpdate(action: String) {
    val intent = Intent(action)
    sendBroadcast(intent)
}

class BluetoothLeService : Service() {

    private var connectionState = STATE_DISCONNECTED

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }
    }

    ...

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }
}

// Broadcast receiver
class DeviceControlActivity : AppCompatActivity() {

    ...
    
        private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothLeService.ACTION_GATT_CONNECTED -> {
                        connected = true
                        updateConnectionState(R.string.connected)
                    }
                    BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                        connected = false
                        updateConnectionState(R.string.disconnected)
                    }
                }
            }
        }
    
        override fun onResume() {
            super.onResume()
            registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
            if (bluetoothService != null) {
                val result = bluetoothService!!.connect(deviceAddress)
                Log.d(DeviceControlsActivity.TAG, "Connect request result=$result")
            }
        }
    
        override fun onPause() {
            super.onPause()
            unregisterReceiver(gattUpdateReceiver)
        }
    
        private fun makeGattUpdateIntentFilter(): IntentFilter? {
            return IntentFilter().apply {
                addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
                addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            }
        }
    }

// close gatt connection
    class BluetoothLeService : Service() {

...

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }
}