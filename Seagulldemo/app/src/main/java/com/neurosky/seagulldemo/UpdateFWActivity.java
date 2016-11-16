package com.neurosky.seagulldemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGdownResult;
/**
 * note : This is the simplest example for firmware update.
 * You have to consider more use case, such as text messages, phone, because this task can not be interrupted.
 * You may have to start a service to operate the data in  background, and launch a notification to make it running till the task finish. 
 */
public class UpdateFWActivity extends BaseActivity {
	private static final String TAG = UpdateFWActivity.class.getSimpleName();
	private TGBleManager tgBleManager = null;
	private SeagullDevice device = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fw_view);
		
		initUI();
	}

	private Button btn_start = null;
	private TextView tv_in_transfer = null;
	private TextView tv_transfer_percent = null;
	private TextView tv_transfer_result = null;
	private TextView tv_transfer_checksum = null;
	private TextView tv_transfer_size = null;
	private TextView tv_transfer_transfer = null;
	private void initUI() {
		// TODO Auto-generated method stub
		btn_start = (Button) findViewById(R.id.btn_start);
		tv_in_transfer = (TextView) findViewById(R.id.tv_in_transfer);
		tv_transfer_percent = (TextView) findViewById(R.id.tv_transfer_percent);
		tv_transfer_result = (TextView) findViewById(R.id.tv_transfer_result);
		tv_transfer_checksum = (TextView) findViewById(R.id.tv_transfer_checksum);
		tv_transfer_size = (TextView) findViewById(R.id.tv_transfer_size);
		tv_transfer_transfer = (TextView) findViewById(R.id.tv_transfer_transfer);
		setListener();
		
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btn_start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(device != null && device.isBond()){
					device.disableFWfilter(); // v1.0.1 firmware can update to v1.0.0 
					//device.enableFWfilter(); // v1.0.1 firmware can not update to v1.0.0
					device.fwDown(); // use the default path and default file name:  /sdcard/nskfw.bin
					//device.fwDown(filePath, fileName);
				}else{
					setBondCallback(new ICallbackForBond(){

						@Override
						public void callFunction() {
							
							device.disableFWfilter(); // v1.0.1 firmware can update to v1.0.0 
							//device.enableFWfilter(); // v1.0.1 firmware can not update to v1.0.0
							device.fwDown(); // use the default path and default file name:  /sdcard/nskfw.bin
						}
						
					});
					connectToDevice();
					//Toast.makeText(getApplicationContext(), "Please connect to band first", Toast.LENGTH_SHORT).show();
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

				case SeagullDevice.MSG_FW_TRANSFER_IN_PROGRESS:
					if(msg.arg1 == 1){
						tv_in_transfer.setText("true");
					}else{
						tv_in_transfer.setText("false");
					}
					
					break;
				case SeagullDevice.MSG_FW_TRANSFER_PERCENT:
					tv_transfer_percent.setText(""+msg.arg1);
					
					break;
				case SeagullDevice.MSG_FW_TRANSFER_REPORT:
					if(msg.arg1 == SeagullDevice.MSG_FW_TRANSFER_REPORT_CHECKSUM ){
						tv_transfer_checksum.setText("" + msg.obj);
						
					}else if(msg.arg1 ==SeagullDevice.MSG_FW_TRANSFER_REPORT_RESULT ){
						tv_transfer_result.setText("" + msg.obj);
						handleFWResult((TGdownResult)msg.obj);
						
					}else if(msg.arg1 ==SeagullDevice.MSG_FW_TRANSFER_REPORT_SIZE ){
						tv_transfer_size.setText("" + msg.obj );
					}else if(msg.arg1 ==SeagullDevice.MSG_FW_TRANSFER_REPORT_TRANSFER){
						tv_transfer_transfer.setText("" + msg.obj);
					}
					break;
	
			}
			super.handleMessage(msg);
		}

	};

	protected void handleFWResult(TGdownResult obj) {
		// TODO Auto-generated method stub
		switch(obj){
		case TGdownResultNormal: //Operation success
			break;
		case TGdownResultDisconnect: //Unexpected event, please contact NeuroSky Support
			break;
		case TGdownResultNoFileFound://Firmware not found, please check firmware file location
			Toast.makeText(getApplicationContext(), "Can't find the fw file", Toast.LENGTH_SHORT).show();
			break;
		case TGdownResultBatteryTooLowForDownload: //Battery level is less than 25%
			break;
		case TGdownResultDisconnectWriteFailed: //Please check connection, retry 
			break;
		case TGdownResultWriteFailed: // do not used yet
			break;
		case TGdownResultWriteTimeOut: //Check if other operation interfere, retry
			break;
		case TGdownResultErrorInvalidImageFile: //Check image file, retry
			break;
			default:
				break;
			
		}
	}
	
}
