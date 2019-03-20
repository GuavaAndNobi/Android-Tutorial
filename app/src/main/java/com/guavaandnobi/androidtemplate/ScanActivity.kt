package com.guavaandnobi.androidtemplate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.ListView
import android.widget.Toast
import com.guavaandnobi.androidtemplate.bluetooth.BLEDeviceAdapter

class ScanActivity : AppCompatActivity() {
    private val scanningPeriod = 10000
    private val devices: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var adapter: BLEDeviceAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private lateinit var scanList: ListView
    private var isStop = false
    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if(!devices.contains(result.device)) {
                devices.add(result.device)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        adapter = BLEDeviceAdapter(this, R.layout.adapter_ble_device, devices)
        scanList = findViewById(R.id.list_scan)
        if(BluetoothAdapter.getDefaultAdapter() != null) {
            bleScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        }
        else {
            Toast.makeText(applicationContext, "Your device does not support Bluetooth", Toast.LENGTH_LONG).show()
            finish()
        }
        scanList.adapter = adapter
        scanList.setOnItemClickListener { _, _, position, _ ->
            val d = devices[position]
            intent.putExtra("Device", d)
            bleScanner.stopScan(scanCallback)
            setResult(1, intent)
            isStop = true
            finish()
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    1001
                )
            }
            else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    1001
                )
            }
        }
        else {
            scanDevice()
        }
    }

    private fun scanDevice() {
        Handler().postDelayed({
            if(this.isFinishing) {}
            else {
                Toast.makeText(this@ScanActivity, "Scan Finished", Toast.LENGTH_LONG).show()
                bleScanner.stopScan(scanCallback)
            }
        }, scanningPeriod.toLong())

        bleScanner.startScan(scanCallback)
        Toast.makeText(this, "Start Scanning", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1001 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    scanDevice()
                } else {
                    finish()
                }
                return
            }
        }
    }
}
