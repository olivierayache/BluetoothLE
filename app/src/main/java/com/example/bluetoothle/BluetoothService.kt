package com.example.bluetoothle

import android.app.Service
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BluetoothService : Service() {

    lateinit var companionDeviceManager: CompanionDeviceManager
    lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService {
            return this@BluetoothService
        }
    }

    override fun onCreate() {
        Log.i("BluetoothService", "creating bluetooth service")
    }

    override fun onDestroy() {
        Log.i("BlService", "Destroyed")
    }

    fun start(deviceName: String, scanCallback: ScanCallback) {
        bluetoothLeScanner.startScan(
            listOf(ScanFilter.Builder().setDeviceName(deviceName).build()),
            ScanSettings.Builder().build(),
            scanCallback
        )
    }

    fun associate(pairingActivity: PairingActivity, uuid: String, callback: CompanionDeviceManager.Callback) {
        companionDeviceManager = pairingActivity.getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        companionDeviceManager.associations.forEach {
            Log.i("BlService", it)
            companionDeviceManager.disassociate(it)
        }

        companionDeviceManager.associate(
            AssociationRequest.Builder()
                .setSingleDevice(false)
                .addDeviceFilter(BluetoothLeDeviceFilter.Builder().build())
                .build(),
            callback,
            null
        )
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}