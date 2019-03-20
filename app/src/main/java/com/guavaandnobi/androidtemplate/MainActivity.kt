package com.guavaandnobi.androidtemplate

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.guavaandnobi.androidtemplate.bluetooth.BLEService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // Properties
    private lateinit var mService: BLEService
    private var mBound = false
    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as BLEService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Intent(applicationContext, BLEService::class.java).also { intent-> startService(intent) }

        btn_scan_main.setOnClickListener {
            Intent(this, ScanActivity::class.java).also { intent->
                startActivityForResult(intent, 0) // requestCode will be modify later
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(mBound) {
            unbindService(mConnection)
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BLEService::class.java).also { intent->
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            0-> if(resultCode == 1) mService.bleConnect(data?.getParcelableExtra("Device") as BluetoothDevice)
        }
    }
}
