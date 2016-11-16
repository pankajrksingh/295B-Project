package com.neurosky.seagulldemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGsyncResult;
import com.neurosky.blecommunication.base.TGBleHandler;
//import com.neurosky.motion.NeuroSkyMotion;
//import com.neurosky.motion.NeuroSkyMotionCallback;

public class SyncDataActivity extends BaseActivity {
	private static final String TAG = SyncDataActivity.class.getSimpleName();
	private TGBleManager manager = null;
	private SeagullDevice device = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_view);
		initUI();
		
		//************************ motion sdk start ************************//
//		neuroskyMotion = new NeuroSkyMotion(motionCallback);
//		Log.d(TAG,"NeuroSky Android Motion Algorithm SDK version: " + neuroskyMotion.getVersion() + "\nAlgorithm version---" + neuroskyMotion.getAlgoVersion() + "\n");
//		
//		/* Initialize sleep analyze */
//		neuroskyMotion.sleepInitAnalysis();
//		neuroskyMotion.enableLogCatMessages(false);
		
		simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		//************************ motion sdk end ************************//
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
		manager = getTGBleManager();
		device = getDevice();
		
		manager.setHandler(msgHandler);
		device.setTGBleHandler(callback);
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
    private Button btn_sync = null;
    private TextView tv_sync_ped_avail = null;
    private TextView tv_sync_ped_recv = null;
    private TextView tv_sync_ecg_avail = null;
    private TextView tv_sync_ecg_recv = null;
    private TextView tv_sync_diag_avail = null;
    private TextView tv_sync_diag_recv = null;
    private TextView tv_sync_sleep_avail = null;
    private TextView tv_sync_sleep_recv = null;
    private TextView tv_sync_result = null;
    private TextView tv_sync_percent = null;
    private TextView  tv_sync_erase_result = null;
    private TextView  tv_sleep_result = null;
    
	private void initUI() {
		// TODO Auto-generated method stub
		tv_sync_percent = (TextView) findViewById(R.id.tv_sync_percent);
		tv_sync_ped_avail = (TextView) findViewById(R.id.tv_sync_ped_avail);
		tv_sync_ped_recv = (TextView) findViewById(R.id.tv_sync_ped_recv);
		tv_sync_ecg_avail = (TextView) findViewById(R.id.tv_sync_ecg_avail);
		tv_sync_ecg_recv = (TextView) findViewById(R.id.tv_sync_ecg_recv);
		tv_sync_diag_avail = (TextView) findViewById(R.id.tv_sync_diag_avail);
		tv_sync_diag_recv = (TextView) findViewById(R.id.tv_sync_diag_recv);
		tv_sync_sleep_avail = (TextView) findViewById(R.id.tv_sync_sleep_avail);
		tv_sync_sleep_recv = (TextView) findViewById(R.id.tv_sync_sleep_recv);
		tv_sync_result = (TextView) findViewById(R.id.tv_sync_result);
		btn_sync = (Button) findViewById(R.id.btn_sync);
		tv_sync_erase_result = (TextView) findViewById(R.id.tv_sync_erase_result);
		tv_sleep_result= (TextView) findViewById(R.id.tv_sleep_result);
		setListener();
	}
	private void setListener(){
		btn_sync.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(device.isBond()){
					device.startSyncData();
				}else{
					//connect and start sync data
					
					setBondCallback(new ICallbackForBond(){

						@Override
						public void callFunction() {
							// TODO Auto-generated method stub
							device.startSyncData();
						}
						
					});
					connectToDevice();
				}
				startFlag = true;
			}
			
		});
	}
	
	private Handler msgHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){

				case SeagullDevice.MSG_HISTORY_ECG:
					handleHistoryECG(msg.arg1,msg.arg2,msg.obj);
					break;
				case SeagullDevice.MSG_TRANSFER_IN_PROGRESS:
					break;
				case SeagullDevice.MSG_TRANSFER_PERCENT:
					tv_sync_percent.setText("" + msg.arg1);
					break;
				case SeagullDevice.MSG_TRANSFER_REPORT:
					handleTransferReport(msg.arg1,msg.arg2,msg.obj);
					
					break;	
					//exceptions include 
					//TGBleCurrentCountRequestTimedOut
					//TGBleHistoryCorruptErased
				case SeagullDevice.MSG_EXCEPTION_TYPE: 
					Log.d(TAG,"Exception:  " + msg.obj);
					
				case SeagullDevice.MSG_DATA_ERASED: 
					Log.d(TAG,"MSG_DATA_ERASED: "+msg.obj.toString());
					tv_sync_erase_result.setText(msg.obj.toString());
					break;
				case SeagullDevice.MSG_BATTERY_LEVEL:
					Log.d(TAG,"Battery info: " + msg.arg1);
					break;
					
			}
			super.handleMessage(msg);
		}

	};
	

	protected void handleHistoryECG(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_HISTORY_ECG_TS:
			Log.d(TAG,"MSG_HISTORY_ECG_TS:" + obj);
			break;
		case SeagullDevice.MSG_HISTORY_ECG_SAMPLERATE:
			break;
		case SeagullDevice.MSG_HISTORY_ECG_COMMENT:
			Log.d(TAG,"MSG_HISTORY_ECG_COMMENT:" + obj);
			break;
			/*  this will handle in TGBleHandler
		case SeagullDevice.MSG_HISTORY_ECG_SAMPLE:
			break;
			*/
		case SeagullDevice.MSG_HISTORY_ECG_STOP:
			break;
		case SeagullDevice.MSG_HISTORY_ECG_FINALHR:
			break;
		}
	}


	protected void handleTransferReport(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_SYNC_PED_AVAIL:
			tv_sync_ped_avail.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_PED_RECV:
			tv_sync_ped_recv.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_ECG_AVAIL:
			tv_sync_ecg_avail.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_ECG_RECV:
			tv_sync_ecg_recv.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_DIAG_AVAIL:
			tv_sync_diag_avail.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_DIAG_RECV:
			tv_sync_diag_recv.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_SLEEP_AVAIL:
			tv_sync_sleep_avail.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_SLEEP_RECV:
			tv_sync_sleep_recv.setText("" + arg2);
			break;
		case SeagullDevice.MSG_SYNC_RESULT:
			tv_sync_result.setText("" + obj);
			handleSyncResult((TGsyncResult)obj);
			
			if(!startFlag){ // Motion SDK 
				/* Request sleep analyze */
				startFlag = true;
//				neuroskyMotion.sleepRequestAnalysis(startDate, tmpDate);
//				Log.e(TAG,"sleepRequestAnalysis called");
				/* Request sleep data downsample */
				//neuroskyMotion.sleepSetInterval(1, false);
			}
			break;
		}
		
	}
	

	private void handleSyncResult(TGsyncResult obj) {
		// TODO Auto-generated method stub
		switch(obj){
		case TGsyncResultNormal: // successful
			device.eraseData(); // call eraseData when sync successful
			
			break;
		case TGsyncResultError: // Unexpected error, please contact NeuroSky Support
			break;
		case TGsyncResultDisconnect: // Check connection and retry
			break;
		case TGsyncResultErrorFirmwareNotCompatible: // Please check firmware version, haven't used yet
			break;
		case TGsyncResultErrorTimeOut: // Timeout, retry
			break;
		case TGsyncResultErrorBlankData:// If multiple blank data been receive, user may needs to reset band
			break;
		case TGsyncResultErrorDataLost: // Lost some data, retry
			break;
		case TGsyncResultErrorFlashCorrupt: // Reset band and retry
			break;
		case TGsyncResultAbort:  // The sync process is interrupted by calling disconnect() or close()
			break;
		}
		
	}
	private TGBleHandler callback = new TGBleHandler(){
		
		/**
		 * MSG_HISTORY_ECG, MSG_HISTORY_ECG_SAMPLE,int<br/>	 
		 * @param callback
		 */

		@Override
		public void onDataReceived(int what, int arg1, int arg2, Object obj) {
			// TODO Auto-generated method stub
			switch(what){
			case SeagullDevice.MSG_HISTORY_ECG:
				handleHistoryECG(arg1,arg2,obj);
				break;
				
			case SeagullDevice.MSG_HISTORY_PED:
				handlePEDData(arg1,arg2,obj);
				break;
			case SeagullDevice.MSG_HISTORY_SLEEP:
				handleSleepData(arg1,arg2,obj);
				break;
			case SeagullDevice.MSG_HISTORY_FATBURN:
				handleFatData(arg1,arg2,obj);
				break;
			case SeagullDevice.MSG_HISTORY_DIAGEVENT:
				handleDiaData(arg1,arg2,obj);
				break;
			}
			
		}
		
	};

	protected void handlePEDData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_HISTORY_PED_TS:
			break;
		case SeagullDevice.MSG_HISTORY_PED_STEPS:
			break;
		case SeagullDevice.MSG_HISTORY_PED_CALORIES:
			break;
		case SeagullDevice.MSG_HISTORY_PED_DISTANCE:
			break;
		case SeagullDevice.MSG_HISTORY_PED_ACTCALORIES:
			break;
		case SeagullDevice.MSG_HISTORY_PED_STEPBPM:
			break;
		case SeagullDevice.MSG_HISTORY_PED_ENERGY:
			break;
		case SeagullDevice.MSG_HISTORY_PED_MODE:
			break;
		case SeagullDevice.MSG_HISTORY_PED_SLEEPPHASE:
			break;
		}
		
	}
	

	/**
	 * * MSG_HISTORY_DIAGEVENT, MSG_HISTORY_DIAGEVENT_TS,string<br/>
		 * MSG_HISTORY_DIAGEVENT, MSG_HISTORY_DIAGEVENT_CODE, int<br/>
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */
	protected void handleDiaData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_HISTORY_DIAGEVENT_TS:
			break;
		case SeagullDevice.MSG_HISTORY_DIAGEVENT_CODE:
			break;
		}
	}

	/**
	 		 * MSG_HISTORY_FATBURN, MSG_HISTORY_FATBURN_TS, string<br/>
		 * MSG_HISTORY_FATBURN, MSG_HISTORY_FATBURN_HR, int<br/>
		 * MSG_HISTORY_FATBURN, MSG_HISTORY_FATBURN_VALUE,int<br/>
		 * MSG_HISTORY_FATBURN, MSG_HISTORY_FATBURN_TRAININGZONE, int<br/>
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */
    protected void handleFatData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
    	switch(arg1){
		case SeagullDevice.MSG_HISTORY_FATBURN_TS:
			break;
		case SeagullDevice.MSG_HISTORY_FATBURN_HR:
			break;
		case SeagullDevice.MSG_HISTORY_FATBURN_VALUE:
			break;
		case SeagullDevice.MSG_HISTORY_FATBURN_TRAININGZONE:
			break;
    	}
	}
    private String dateString;
    private String[] dateStrings ;
	/**
     * MSG_HISTORY_SLEEP, MSG_HISTORY_SLEEP_TS, string<br/>
		 * MSG_HISTORY_SLEEP, MSG_HISTORY_SLEEP_SLEEPPHASE,int<br/>
		 * MSG_HISTORY_SLEEP, MSG_HISTORY_SLEEP_INITCODE,int<br/>
     * @param arg1
     * @param arg2
     * @param obj
     */
	protected void handleSleepData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		switch(arg1){
		case SeagullDevice.MSG_HISTORY_SLEEP_TS:
			try {
				dateString = obj.toString();
				dateStrings = dateString.split("//+");
				tmpDate = simpleDateFormat.parse(dateStrings[0]);
				if(startFlag){
					startFlag = false;
					startDate = tmpDate;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				tmpDate = null;
			}
			break;
		case SeagullDevice.MSG_HISTORY_SLEEP_SLEEPPHASE:
			tmpPhase = arg2;
			// TODO:remove motion SDK
//			neuroskyMotion.sleepAddData(tmpDate, tmpPhase);

			break;
		case SeagullDevice.MSG_HISTORY_SLEEP_INITCODE:
			break;
		}
	}
	
	
	/*********************/
//	private NeuroSkyMotion neuroskyMotion;
	private SimpleDateFormat simpleDateFormat ;
	private Date tmpDate = null;
	private int tmpPhase = -1;
	private Date startDate = null;
	private Date endDate = null;
	private boolean startFlag = true;
	
	/**
	 * Sleep algorithm callback 
	 */
//	public NeuroSkyMotionCallback motionCallback = new NeuroSkyMotionCallback() {
//
//		/**
//		 * This callback interface will return the sleep analyze return.
//		 * @param result sleep analyze result: 0 - valid, 1 - Not valid, 2 - No data, 3 - Negative time, 4 - out of range, 5 - Analyze in progress
//		 * @param startTime sleep analyze start time
//		 * @param endTime sleep analyze end time
//		 * @param duration minutes of sleep analyze session
//		 * @param preSleep minutes before fall into sleep
//		 * @param notSleep minutes not in sleep
//		 * @param deepSleep minutes in deep sleep
//		 * @param lightSleep minutes in light sleep
//		 * @param wakeUpCount wake up times
//		 * @param totalSleep minutes that falls into sleep
//		 * @param preWake minutes before not sleep before sleep analyze session end
//		 * @param efficiency sleep efficiency
//		 */
//		@Override
//		public void sleepResults(int result, Date startTime, Date endTime,
//				int duration, int preSleep, int notSleep, int deepSleep,
//				int lightSleep, int wakeUpCount, int totalSleep, int preWake,
//				int efficiency) {
//			// TODO Auto-generated method stub
//			tv_sleep_result.append("Sleep analysis result: " + result + "\n");
//			tv_sleep_result.append("Sleep analysis start time: " + startTime.getTime()/1000 + "\n");
//			tv_sleep_result.append("Sleep analysis end time: " + endTime.getTime()/1000 + "\n");
//			tv_sleep_result.append("Sleep analysis duration: " + duration + "\n");
//			tv_sleep_result.append("Sleep analysis pre sleep: " + preSleep + "\n");
//			tv_sleep_result.append("Sleep analysis not sleep: " + notSleep + "\n");
//			tv_sleep_result.append("Sleep analysis deep sleep: " + deepSleep + "\n");
//			tv_sleep_result.append("Sleep analysis light sleep: " + lightSleep + "\n");
//			tv_sleep_result.append("Sleep analysis wake up count: " + wakeUpCount + "\n");
//			tv_sleep_result.append("Sleep analysis totalSleep: " + totalSleep + "\n");
//			tv_sleep_result.append("Sleep analysis pre wake: " + preWake + "\n");
//			tv_sleep_result.append("Sleep analysis efficiency: " + efficiency + "\n");
//		}
//
//		/**
//		 * This callback interface will return downsample sleep transition.
//		 */
//		@Override
//		public void sleepSmoothData(int minutes, Date[] sleepTimeArr,
//				int[] sleepPhaseArr) {
//			// TODO Auto-generated method stub
//			runOnUiThread(new getSleepSmoothData(sleepTimeArr, sleepPhaseArr));
//		}
//		
//	};
	
	/**
	 * Print sleep downsample result on UI
	 */
	public class getSleepSmoothData implements Runnable {

		Date[] time;
		int[]  phase;
		
		public getSleepSmoothData(Date[] t, int[] p) {
			time = t;
			phase = p;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG,"Sleep Smooth Data length: " +time.length );
			for(int i = 0; i < time.length; i++){
				Log.d(TAG,"Sleep Smooth Data TS: " + time[i].getTime()/1000 + ", phase: " + phase[i] + "\n");
			}
		}
		
	}

}
