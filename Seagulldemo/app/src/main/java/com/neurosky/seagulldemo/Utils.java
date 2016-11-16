package com.neurosky.seagulldemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Utils {
	
	public static void checkBluetooth(Context context){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter == null ){
			Toast.makeText(context, "Do not support Bluetooth", Toast.LENGTH_SHORT).show();
		}else if(!adapter.isEnabled()){
			Intent intent =  new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			context.startActivity(intent);
		}
	}

}
