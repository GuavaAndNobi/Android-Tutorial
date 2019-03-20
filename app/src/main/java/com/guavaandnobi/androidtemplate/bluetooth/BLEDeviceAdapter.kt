package com.guavaandnobi.androidtemplate.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.guavaandnobi.androidtemplate.R

class BLEDeviceAdapter (context: Context, private val resource: Int, private val items: MutableList<BluetoothDevice>)
    : ArrayAdapter<BluetoothDevice>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: LinearLayout
        val item = getItem(position)

        if (convertView == null) {
            itemView = LinearLayout(context)
            val inflater = Context.LAYOUT_INFLATER_SERVICE
            val li = context.getSystemService(inflater) as LayoutInflater
            li.inflate(resource, itemView, true)
        } else {
            itemView = convertView as LinearLayout
        }

        val name: TextView = itemView.findViewById(R.id.text_ble_adapter_name)
        val address: TextView = itemView.findViewById(R.id.text_ble_adapter_address)
        name.text = if(item != null) if(item.name != null) item.name else "Unknown" else "Unknown"
        address.text = if(item != null) item.address else "Unknown"

        return itemView
    }

    operator fun set(index: Int, item: BluetoothDevice) {
        if (index >= 0 && index < items.size) {
            items[index] = item
            notifyDataSetChanged()
        }
    }

    operator fun get(index: Int): BluetoothDevice {
        return items[index]
    }

}