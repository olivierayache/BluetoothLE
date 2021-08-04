package com.example.bluetoothle

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.companion.CompanionDeviceManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import java.util.*

class PairingActivity : Activity(), ServiceConnection {

    lateinit var bluetoothService: BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pairing_layout)
        startService(Intent(this, BluetoothService::class.java))
        bindService(
            Intent(this, BluetoothService::class.java),
            this,
            BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        //unbindService()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        bluetoothService = (service as BluetoothService.LocalBinder).getService()
        bluetoothService.associate(this, "", object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(chooserLauncher: IntentSender) {
                this@PairingActivity.startIntentSenderForResult(
                    chooserLauncher,
                    42,
                    null,
                    0,
                    0,
                    0
                )
            }

            override fun onFailure(error: CharSequence?) {
                Log.e("PairingActivity", error.toString())
            }

        })
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && data != null) {
            val scanResult = data.extras?.get(CompanionDeviceManager.EXTRA_DEVICE) as ScanResult

            Log.i(PairingActivity::class.java.name, Arrays.toString(scanResult.scanRecord?.serviceData?.values?.first()))

            scanResult.device.connectGatt(this, false, object : BluetoothGattCallback() {

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                        val bc = BluetoothGattCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"), 2, 0)
                        gatt?.setCharacteristicNotification(bc, true)
                        gatt?.discoverServices()
                    }


                }


                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    gatt?.services?.forEach {
                        it.characteristics.forEach {
                            Log.i("uuid", it.uuid.toString())
                            Log.i("p", it.properties.toString())
                            Log.i("perm", it.permissions.toString())
                            //gatt.setCharacteristicNotification(it, true)
                        }
                    }
                    Log.i("Pairing Activity", gatt?.services.toString())
                    val bc = BluetoothGattCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"), 2, 0)
                    gatt?.setCharacteristicNotification(bc, true)
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?
                ) {
                    Log.i("Pairing Activity", "hello")
                }

                override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                    Log.i("Pairing Activity", "hello")

                }

                override fun onDescriptorWrite(
                    gatt: BluetoothGatt?,
                    descriptor: BluetoothGattDescriptor?,
                    status: Int
                ) {
                    Log.i("Pairing Activity", "hello")

                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    Log.i("Pairing Activity", "write")
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    Log.i("Pairing Activity", "read")
                }


            })
        }
    }
}