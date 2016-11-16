package com.neurosky.seagulldemo;

import java.util.Date;

import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGsendResult;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetGoalActivity extends BaseActivity {
	private static final String TAG = SetGoalActivity.class.getSimpleName();
	private TGBleManager tgBleManager = null;
	private SeagullDevice device = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_goal_view);
		initUI();
	}
	
	private Button btn_set = null;
	private Button btn_cancel = null;
	
	private EditText et_sg_alarmhour = null;
	private EditText et_sg_steps = null;
	private EditText et_sg_alarmminute = null;
	private EditText et_sg_alarmrepeat = null;
	private EditText et_sg_duration_hour = null;
	private EditText et_sg_duration_min = null;
	private EditText et_sg_duration_sec = null;

	
	
	private void initUI() {
		// TODO Auto-generated method stub
		et_sg_alarmhour = (EditText)findViewById(R.id.et_sg_alarmhour);
		
		et_sg_steps = (EditText)findViewById(R.id.et_sg_steps);
		
		et_sg_alarmminute = (EditText)findViewById(R.id.et_sg_alarmminute);
		
		et_sg_alarmrepeat = (EditText)findViewById(R.id.et_sg_alarmrepeat);
		
		et_sg_duration_hour = (EditText)findViewById(R.id.et_sg_duration_hour);
		
		et_sg_duration_min = (EditText)findViewById(R.id.et_sg_duration_min);
		
		et_sg_duration_sec = (EditText)findViewById(R.id.et_sg_duration_sec);
		
		
		btn_set = (Button) findViewById(R.id.btn_set);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		setListener();
	
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btn_set.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(device != null && device.isBond()){
					setValue();
				}else{
					
					setBondCallback(new ICallbackForBond(){

						@Override
						public void callFunction() {
							// TODO Auto-generated method stub
							setValue();
						}
						
					});
					connectToDevice();
					
				}
			}
			
		});
		
		btn_cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
	}
	
	private void getValue(){
		et_sg_alarmhour.setText(""+device.getAlarmHour());
		et_sg_steps.setText("" + device.getGoalSteps());
		et_sg_alarmminute.setText("" + device.getAlarmMinute());
		et_sg_alarmrepeat.setText("" + device.getAlarmRepeat());
		et_sg_duration_hour.setText("" + device.getGoalDurationHour());
		et_sg_duration_min.setText("" + device.getGoalDurationMinutes());
		et_sg_duration_sec.setText("" + device.getGoalDurationSecond());
		
	}
	
	private void setValue(){
		
		//need check the value first
		// ...
		
		device.setAlarmHour(Integer.parseInt(et_sg_alarmhour.getText().toString()) );
		device.setAlarmMinute(Integer.parseInt(et_sg_alarmminute.getText().toString()) );
		device.setAlarmRepeat(Integer.parseInt(et_sg_alarmrepeat.getText().toString()) );
		
		int hour = Integer.parseInt(et_sg_duration_hour.getText().toString());
		int min = Integer.parseInt(et_sg_duration_min.getText().toString());
		int second = Integer.parseInt(et_sg_duration_sec.getText().toString());
		Date date = new Date();
		int newSecond = date.getSeconds() + second;
		if(newSecond > 59){
			newSecond = newSecond - 60;
			min ++;
		}
		int newMin = date.getMinutes() + min;
		if(newMin >59){
			newMin = newMin - 60;
			hour ++;
		}
		int newHour = date.getHours() + hour; // how about later than 23 ??
		device.setGoalDurationHour( newHour );
		device.setGoalDurationMinute( newMin);
		device.setGoalDurationSecond(newSecond );
		device.setGoalSteps(Integer.parseInt(et_sg_steps.getText().toString()) );
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
		getValue();
	}
	
	private Handler msgHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){

				case SeagullDevice.MSG_USER_GOAL:
					handleSetGoalResult((TGsendResult)msg.obj);
					break;
	
			}
			super.handleMessage(msg);
		}

	};
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	protected void handleSetGoalResult(TGsendResult obj) {
		// TODO Auto-generated method stub
		Log.d(TAG," set goal result: " + obj);
		switch(obj){
		case TGSendSuccess:
			Toast.makeText(getApplicationContext(), "Goal set Successful", Toast.LENGTH_SHORT).show();
			break;
		case TGSendFailedNoAcknowledgement:
			break;
		case TGSendFailedDisconnected:
			break;
		default:
			break;
		}
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
