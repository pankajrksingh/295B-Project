package com.neurosky.seagulldemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;

public class RealtimeSportActivity extends BaseActivity {
	private static final String TAG = RealtimeSportActivity.class.getSimpleName();
	private TGBleManager tgBleManager = null;
	private SeagullDevice device = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.realtime_sport_view);
		initUI();
		
	}

	private Button btn_start = null;
	private Button btn_stop = null;
	
	private TextView tv_sp_ts = null;
	private TextView tv_sp_steps = null;
	private TextView tv_sp_calories = null;
	private TextView tv_sp_distance = null;
	private TextView tv_sp_energy = null;
	private TextView tv_sp_mode = null;
	private TextView tv_sp_hr = null;
	private TextView tv_sp_actcalories = null;
	
	private void initUI() {
		// TODO Auto-generated method stub
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		
		tv_sp_ts = (TextView) findViewById(R.id.tv_sp_ts);
		tv_sp_steps = (TextView) findViewById(R.id.tv_sp_steps);
		tv_sp_calories = (TextView) findViewById(R.id.tv_sp_calories);
		tv_sp_distance = (TextView) findViewById(R.id.tv_sp_distance);
		tv_sp_energy = (TextView) findViewById(R.id.tv_sp_energy);
		tv_sp_mode = (TextView) findViewById(R.id.tv_sp_mode);
		tv_sp_hr = (TextView) findViewById(R.id.tv_sp_hr);
		tv_sp_actcalories = (TextView) findViewById(R.id.tv_sp_actcalories);
		
		setListener();
		
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btn_start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(device != null && device.isBond()){
					device.startRealTimeSport();
				}else{
					setBondCallback(new ICallbackForBond(){

						@Override
						public void callFunction() {
							// TODO Auto-generated method stub
							device.startRealTimeSport();
						}
						
					});
					connectToDevice();
					//Toast.makeText(getApplicationContext(), "Please connect to band first", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		btn_stop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(device != null && device.isBond()){
					device.stopRealTimeSport();
				}
			}
			
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
		tgBleManager = getTGBleManager();
		device = getDevice();
		
		tgBleManager.setHandler(msgHandler);
		//device.setTGBleHandler(callback); // fw only need handle
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	private Handler msgHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){

				case SeagullDevice.MSG_REALTIME_COUNT:
					handleRealtimeCountData(msg.arg1,msg.arg2,msg.obj);
					
					break;
					//exceptions include 
					//TGBleCurrentCountRequestTimedOut
					//
				case SeagullDevice.MSG_EXCEPTION_TYPE: 
					Log.d(TAG,"Exception:  " + msg.obj);
					
					break;
					
				case SeagullDevice.MSG_BATTERY_LEVEL:
					Log.d(TAG,"Battery info: " + msg.arg1);
					break;
	
			}
			super.handleMessage(msg);
		}

	};
	
	/**
	 * 	
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */

	protected void handleRealtimeCountData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_REALTIME_COUNT_TS:
			Log.d(TAG,"MSG_REALTIME_COUNT_TS:" + obj);
			tv_sp_ts.setText("" + obj);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_STEPS:
			Log.d(TAG,"MSG_REALTIME_COUNT_STEPS:" + arg2);
			tv_sp_steps.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_CALORIES:
			Log.d(TAG,"MSG_REALTIME_COUNT_CALORIES:" + arg2);
			tv_sp_calories.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_DISTANCE:
			Log.d(TAG,"MSG_REALTIME_COUNT_DISTANCE:" + arg2);
			tv_sp_distance.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_ENERGY:
			Log.d(TAG,"MSG_REALTIME_COUNT_ENERGY:" + arg2);
			tv_sp_energy.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_MODE:
			Log.d(TAG,"MSG_REALTIME_COUNT_MODE:" + arg2);
			tv_sp_mode.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_HR:
			Log.d(TAG,"MSG_REALTIME_COUNT_HR:" + arg2);
			tv_sp_hr.setText("" + arg2);
			break;
		case SeagullDevice.MSG_REALTIME_COUNT_ACTCALORIES:
			Log.d(TAG,"MSG_REALTIME_COUNT_ACTCALORIES:" + arg2);
			tv_sp_actcalories.setText("" + arg2);
			break;
		
		}
		
	}

}
