package com.guavaandnobi.androidtemplate.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.util.*

class BLEService : Service() {
    /**
     * Binder for Activity to communicate the Service
     */
    inner class LocalBinder: Binder() {
        fun getService(): BLEService = this@BLEService
    }
    private val mBinder = LocalBinder()

    // Properties
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null
    private val bluetoothGattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when(newState) {
                BluetoothProfile.STATE_CONNECTED-> {
                    this@BLEService.gatt = gatt
                    broadcastUpdate(BLEProfile.BLE_CONNECTED)
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED-> {
                    this@BLEService.gatt = null
                    broadcastUpdate(BLEProfile.BLE_DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if(status == BluetoothGatt.GATT_SUCCESS) broadcastUpdate(BLEProfile.BLE_SERVICE_FOUND)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            broadcastUpdate(BLEProfile.BLE_CHARACTERISTIC_CHANGED, characteristic.value)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private fun broadcastUpdate(intentAction: String) {
        Intent(intentAction).also { intent-> sendBroadcast(intent) }
    }

    private fun broadcastUpdate(intentAction: String, value: ByteArray) {
        Intent(intentAction).also { intent->
            intent.putExtra("Value", value)
            sendBroadcast(intent)
        }
    }

    fun bleConnect(device: BluetoothDevice) {
        if(BluetoothAdapter.getDefaultAdapter() == null) return
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).also { intent-> startActivity(intent)  }
        }
        else {
            this.device = device
            device.connectGatt(this, false, bluetoothGattCallback)
        }
    }

    fun bleDisconnect() {
        gatt?.close()
        gatt = null
        broadcastUpdate(BLEProfile.BLE_DISCONNECTED)
    }

    fun bleEnableNotification(serviceUuidString: String, CharacteristicUuidString: String) {
        if(gatt == null) return
        val service = if(gatt?.getService(UUID.fromString(serviceUuidString)) != null)
            gatt?.getService(UUID.fromString(serviceUuidString)) else {
            bleDisconnect()
            return
        }

        val characteristic = if(service?.getCharacteristic(UUID.fromString(CharacteristicUuidString)) != null)
            service.getCharacteristic(UUID.fromString(CharacteristicUuidString)) else {
            bleDisconnect()
            return
        }

        gatt?.setCharacteristicNotification(characteristic, true)
        val descriptorUUID : UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        val descriptor = characteristic.getDescriptor(descriptorUUID).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }

        gatt?.writeDescriptor(descriptor)
    }
}
