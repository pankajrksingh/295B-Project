package com.neurosky.seagulldemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;
import com.neurosky.blecommunication.base.TGReturnCode;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends BaseActivity {
	private final static String TAG = MainActivity.class.getSimpleName();
	private TGBleManager tgBleManager = null;

	private SimpleDateFormat dateFormatGmt;
	private SeagullDevice seagullDevice = null;

	ArrayList<BluetoothDevice> pairedDeviceArrayList;
	ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
	private UUID myUUID;
	private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
	ListView listViewPairedDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listViewPairedDevice = (ListView)findViewById(R.id.paireddevicelist);
		try {
			client = new MqttClient("tcp://54.244.148.72:1883", MqttClient.generateClientId(), null);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		try {
			client.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}

		setContentView(R.layout.first_view);
		initUI();
		dateFormatGmt = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// call super.onResume() first
		tgBleManager = getTGBleManager();
		seagullDevice = getDevice();
		tgBleManager.setHandler(mHandler);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (tgBleManager != null) {
			// tgBleManager.getEventBus().unregister(this);
			tgBleManager.disconnect();
			tgBleManager.close();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private static final int MSG_UPDATE_LOGS = 1000;

	// ********************* Using handler to communicate
	// *****************************//
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {

			case MSG_UPDATE_LOGS:
				tv_log.append((String) msg.obj);
				sv_show.scrollTo(0, tv_log.getHeight());
				break;

			case SeagullDevice.MSG_BATTERY_LEVEL:
				bigLog("Battery info: " + msg.arg1);
				break;

			}
			super.handleMessage(msg);

		}

	};

	// private TextView tv_state = null;
	private TextView tv_log = null;
	// private TextView tv_raw = null;
	private ScrollView sv_show = null;
	private Button btn_connect = null;
	private Button conn_pill_bottle = null;
	private Button btn_setgoal = null;
	private Button btn_startrealtime = null;
	private Button btn_fwtest = null;
	private Button btn_sync = null;
	private Button btn_close = null;
	private Button btn_getstate = null;
	private Button btn_realtime_sport = null;




	private void connectPillBottle() {
		String deviceName = "HC-06";
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
//			pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

			for (BluetoothDevice device : pairedDevices) {

				ConnectThread myThreadConnectBTdevice = null;
				if (deviceName.equals(device.getName())) {
					Log.i(TAG, device.getName());
					myThreadConnectBTdevice = new ConnectThread(device);
					myThreadConnectBTdevice.start();
				}
			}
		}
	}


	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
		private UUID MY_UUID;

		public ConnectThread(BluetoothDevice device) {
			MY_UUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}

			// Do work to manage the connection (in a separate thread)
//			manageConnectedSocket(mmSocket);
			ReadWriteBLEData manageConnectedSocket = new ReadWriteBLEData(mmSocket);
			manageConnectedSocket.start();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}


	private class ReadWriteBLEData extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ReadWriteBLEData(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					String temp = new String(String.valueOf(bytes));
//					Log.i(TAG, "===========================================Pill Bottle Data=======================================");
//					Log.i(TAG, temp);
//					Log.i(TAG, "===========================================Pill Bottle Data=======================================");
					publishMessage hbpublish = new publishMessage();
					hbpublish.sendMessage(temp, "pilldata");
					// Send the obtained bytes to the UI activity
//					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//							.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}




	private void initUI() {
		// TODO Auto-generated method stub

		// tv_state = (TextView)findViewById(R.id.tv_state);
		// tv_state.setVisibility(View.GONE);

		tv_log = (TextView) findViewById(R.id.tv_log);
		sv_show = (ScrollView) findViewById(R.id.sv_show);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		conn_pill_bottle = (Button) findViewById(R.id.conn_pill_bottle);
		btn_setgoal = (Button) findViewById(R.id.btn_setgoal);
		btn_setgoal.setText("Goal");
		btn_startrealtime = (Button) findViewById(R.id.btn_startrealtime);
		btn_fwtest = (Button) findViewById(R.id.btn_fwtest);
		btn_sync = (Button) findViewById(R.id.btn_sync);
		btn_close = (Button) findViewById(R.id.btn_close);
		btn_getstate = (Button) findViewById(R.id.btn_getstate);
		btn_getstate.setVisibility(View.GONE);

		btn_realtime_sport = (Button) findViewById(R.id.btn_realtime_sport);
		btn_realtime_sport.setText("Rel Sport");
		// tv_raw= (TextView)findViewById(R.id.tv_raw);
		// tv_raw.setVisibility(View.GONE);
		setListener();
	}




	private void setListener() {
		// TODO Auto-generated method stub
		btn_connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				connectToDevice();
			}
		});

		conn_pill_bottle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "conn_pill_bottle clicked");
				connectPillBottle();
			}
		});
		btn_realtime_sport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(MainActivity.this, RealtimeSportActivity.class);
				startActivity(intent);
			}
		});

		btn_setgoal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, SetGoalActivity.class);
				startActivity(intent);
			}
		});
		btn_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				bigLog("btn_close click");
				if (tgBleManager != null) {
					tgBleManager.close();
				}

			}
		});

		btn_startrealtime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i("Algo3p0", "RealtimeECGActivity start!!");
				Intent intent = new Intent(MainActivity.this, RealtimeECGActivity.class);
				startActivity(intent);

				// if(seagullDevice != null){
				// bigLog("seagullDevice.startRealTimeECG()" +
				// seagullDevice.startRealTimeECG() );
				// }

			}
		});

		btn_fwtest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, UpdateFWActivity.class);
				startActivity(intent);

			}
		});

		btn_sync.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, SyncDataActivity.class);
				startActivity(intent);

			}
		});
		btn_getstate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	TGReturnCode result;




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	private void bigLog(String msg) {
		Log.d(TAG, msg);

		Message message = new Message();
		message.what = MSG_UPDATE_LOGS;
		message.obj = dateFormatGmt.format(new Date()) + " " + msg + "\n";
		mHandler.sendMessage(message);
	}

}
