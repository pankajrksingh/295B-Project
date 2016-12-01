package com.neurosky.seagulldemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;
import com.neurosky.blecommunication.base.DeviceType;
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGBleExceptionEvent;
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGbondResult;
import com.neurosky.blecommunication.base.TGReturnCode;
import com.neurosky.blecommunication.eventbus.BondEvent;
import com.neurosky.blecommunication.eventbus.ConnectionStateChangedEvent;
import com.neurosky.blecommunication.eventbus.DiscoveredDeviceEvent;
import com.neurosky.blecommunication.eventbus.ExceptionEvent;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * BaseActivity is used as the sdk adapter. You can also use a service.
 * 
 *
 */
public class BaseActivity extends Activity {
	private static final String TAG = BaseActivity.class.getSimpleName();
	private static final String SHAREFILENAME = "comm_demo_file";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    // Using static to keep only one instance
	public static BluetoothAdapter mBluetoothAdapter= null;
	public static BluetoothDevice mBluetoothDevice= null;
	private static TGBleManager tgBleManager = null;
	private static SeagullDevice seagullDevice = null;
	private static Handler handler ;
	private TGReturnCode result ;
	private static ICallbackForBond callback = null;
	
	//show device list while scanning
	private static ListView list_select;
	private static BTDeviceListAdapter deviceListApapter = null;  //multi ConnectionActivity obj
	private static Dialog selectDialog;
	
	private static SharedPreferences sharePreferences;
	private static SharedPreferences.Editor shareEditor;
	
	public static final String SP_PROFILE_BIRTH_DAY = "proifle_birth_day";
	public static final String SP_PROFILE_BIRTH_MONTH = "proifle_birth_month";
	public static final String SP_PROFILE_BIRTH_YEAR = "proifle_birth_year";
	public static MqttClient client = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//init the static members
		if(mBluetoothAdapter  == null){
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        tgBleManager = TGBleManager.getInstance();
	        //initCheck();
	        handler = new Handler(Looper.getMainLooper());
	        sharePreferences = getSharedPreferences(SHAREFILENAME, Activity.MODE_PRIVATE);
	        shareEditor = sharePreferences.edit();
		}
        Log.i(TAG,"onCreate :" + this);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG,"onDestroy :" + this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG,"onPause :" + this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG,"onResume: " + this);
		Utils.checkBluetooth(this); // make sure the BT is on
		initCheck(); 
		 
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG,"EventBus register on: " + this);
		Log.d(TAG,"is main activity? " + (this instanceof MainActivity));
		
		//EventBus here is greenrobot EventBus. For details , please visit 
		//https://github.com/greenrobot/EventBus
		//Make MainActivity lower priority here
		if(this instanceof MainActivity){
			tgBleManager.getEventBus().register(this,0);
		}else{
			tgBleManager.getEventBus().register(this,1);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		tgBleManager.getEventBus().unregister(this);
		Log.d(TAG,"EventBus unregister on: " + this);
		super.onStop();
		
	}
	
	public void initCheck(){
		if(!tgBleManager.isInit()){
			// make sure the device type is DEVICE_TYPE_SEAGULL
	        result =  tgBleManager.initTGBleManager(getApplicationContext(), mBluetoothAdapter, null, DeviceType.DEVICE_TYPE_SEAGULL);
	        if(result == TGReturnCode.SUCCESS ){
	        	seagullDevice = (SeagullDevice)tgBleManager.getDevice();
	        	// output all the sdk log
	        	tgBleManager.setLoggingFlag(true);

	        	// all the logcat log will be redirect to file in /sdcard/neurosky/console_log/
	            TGBleManager.redirectConsoleLogToDocumentFolder();
	            
	            //set the default/local profile, maybe you have to do it before connection
	            int day = sharePreferences.getInt(SP_PROFILE_BIRTH_DAY, 1); //get the value from local storage or remote storage
	            seagullDevice.setBirthDay(day);
	            
	            int month = sharePreferences.getInt(SP_PROFILE_BIRTH_MONTH, 1);
	            seagullDevice.setBirthMonth(month);
	            
	            int year = sharePreferences.getInt(SP_PROFILE_BIRTH_YEAR, 1990);
	            seagullDevice.setBirthYear(year);
	            
	            // set more value below 
	            // ...
	            
	            
	        }else{
	        	Toast.makeText(getApplicationContext(), "TGBleManager init failed: "+result, Toast.LENGTH_SHORT).show();;
	        }
		}
	}
	
	
	//***************************************  using EventBus for communication   ****************************//
		public void onEvent(final DiscoveredDeviceEvent event){
			tgBleManager.getEventBus().cancelEventDelivery(event); // cancel  event delivery, so this event will be handled only once
			Log.e(TAG,"Found device "+event.getDevice().getAddress());
			Log.d(TAG,"DiscoveredDeviceEvent 1 called by"+this);
			handler.post(new Runnable(){
	
				@Override
				public void run() {
					// update the list 
					deviceListApapter.addDevice(event.getDevice(),event.getScanRecord());
					deviceListApapter.notifyDataSetChanged();
					Log.d(TAG,"DiscoveredDeviceEvent 2 called by"+this);
				}
	
			});
	
		}
	
		public void onEvent(final ConnectionStateChangedEvent event){
			tgBleManager.getEventBus().cancelEventDelivery(event);
			Log.d(TAG,"State change  to " + event.getCurState());

			handleStateChanged(event.getCurState());
		}
	


		public void onEvent(final BondEvent event){
			tgBleManager.getEventBus().cancelEventDelivery(event);
			int eventType = event.getEventType();

			if(SeagullDevice.BLE_DID_BOND ==  eventType ){
				Log.d(TAG,"Bond info:" +event.getResult().toString());
				handleBondResult((TGbondResult)event.getResult());
			}
			if(eventType ==SeagullDevice.POTENTIAL_BOND_CODE ){
				Log.d(TAG,"The code should be " + event.getValue());
				//need a dialog to confirm the code is the same with the one show on band,
				Log.d(TAG,"seagullDevice.takeBond():" +seagullDevice.takeBond());  // tryBond() -> takeBond()
			}
			if(SeagullDevice.POTENTIAL_BOND_SN == eventType ){
				Log.d(TAG,"Bond serial number: " + event.getValue());
			}
			if(SeagullDevice.POTENTIAL_BOND_NAME == eventType ){
				Log.d(TAG,"Bond name: " + event.getValue());
			}
			if(SeagullDevice.BLE_BOND_TOKEN == eventType){
				Log.d(TAG,"Bond token: " + event.getValue());// the token generated by takeBond()
			}
			

		}
		
		public void onEvent(final ExceptionEvent exceptionEvent){
			TGBleExceptionEvent event = (TGBleExceptionEvent) exceptionEvent.getExceptionEvent();
			switch(event){
			case TGBleUserBirthDateRejected_AgeOutOfRange:
				break;
			case TGBleStepGoalRejected_OutOfRange:
				break;
			case TGBleCurrentCountRequestTimedOut: //current count timeout, you can handle this exception in real time sport activity 
				break;
			case TGBleHistoryCorruptErased: //band memory has been corrupted, you can handle this exception in sync activity by call eraseData()
				break;
			case TGBleReInitializedNeedAlarmAndGoalCheck: //Alarm / Goal been re-config, need user attention.
				break;
			case TGBleNonRepeatingAlarmMayBeStaleCheckAlarmConfiguration: //Alarm setting has been updated, need user attention. 
				break;
			default:
				break;
			
			}
		}
		
		private void handleStateChanged(int state) {
			// TODO Auto-generated method stub
			switch(state){
			/**
			 *disconnect() called and  BluetoothGattCallback.onConnectionStateChange() received the disconnected message.
			 *It is also the initial state
			 */
			case TGBleManager.STATE_DISCONNECTED:
				break;
			/**
			 *candidateConnect() called successfully
			 */
			case TGBleManager.STATE_CONNECTING:
				Log.d(TAG," STATE_CONNECTING");
				break;
			/**
			 *candidateConnect() or candidateReConnect() called successfully and  BluetoothGattCallback.onConnectionStateChange() received the connected message.
			 */
			case TGBleManager.STATE_CONNECTED:
				break;
		    /**
		     * disconnect() called
		     */
			case TGBleManager.STATE_DISCONNECTING:
				break;
		    /**
		     * STATE_SERVICE_DISCOVERED indicate that BluetoothGattCallback.onServicesDiscovered() received the success message.
		     */
			case TGBleManager.STATE_SERVICE_DISCOVERED:
				break;
		    /**
		     * connect successfully
		     */
			case TGBleManager.STATE_CONNECT_SUCCESS:
				Log.d(TAG," TGBleManager.STATE_CONNECT_SUCESS");
				
				//basic information can be get from now on
				Log.i(TAG," Serial number: "+seagullDevice.getHwSerialNumber());
				Log.i(TAG," Fw version: "+seagullDevice.getFwVersion());
				Log.i(TAG," HW version: "+seagullDevice.getHwVersion());
				Log.i(TAG," SW version: "+seagullDevice.getSwVersion());
				Log.i(TAG," Manufacturer ID: "+seagullDevice.getMfgId());
				Log.i(TAG," Model Number: "+seagullDevice.getHwModel());
				
				// start the bond process
				if (seagullDevice.getBondToken() == null) {
					
					result =seagullDevice.tryBond();
					Log.d(TAG, " seagullDevice.tryBond: " +result);
					
					//call seagullDevice.takeBond() after you get BondEvent POTENTIAL_BOND_CODE
					
				} else {
					
					result = seagullDevice.adoptBond(seagullDevice.getBondToken(),seagullDevice.getHwSerialNumber());
					Log.d(TAG, "seagullDevice.adoptBond:   " +result);
				}
				showToast("Bonding...");
				break;
		    /**
		     * bond successfully, 
		     */
			case TGBleManager.STATE_BOND_BONDED:
				
				Log.d(TAG,"STATE_BOND_BONDED");
				
//				if(callback != null){
//					callback.callFunction();
//					callback = null;
//				}
				showToast("Bond successful");
				break;
				
				/**
			     * get the first battery level value successful
			     */
			case TGBleManager.STATE_INIT_BAND_SUCCESS:
				
				// run the callback here, 
				Log.d(TAG,"STATE_INIT_BAND_SUCCESS");
				if(callback != null){
					callback.callFunction();
					callback = null;
				}
				showToast("Init band successful");
				break;
		    /**
		     * close() called
		     */
			case TGBleManager.STATE_CLOSED:
				break;
			 /**
		     * candidateConnect() or candidateReConnect() called successfully but  BluetoothGattCallback.onConnectionStateChange() doesn't receive the connected message on time.
		     */
			case TGBleManager.STATE_CONNECTING_TIMEOUT:

				showToast("Connecting failed, timeout");
				break;
			/**
		     * candidateConnect() or candidateReConnect() called successfully but  BluetoothGattCallback.onServicesDiscovered() doesn't receive the success message on time.
		     */
			case TGBleManager.STATE_DISCOVER_SERIVCE_TIMEOUT:

				showToast("Discover service timeout, try again ");
				BluetoothDevice bt_device = tgBleManager.getBluetoothDevice();
				Log.d(TAG,"reconn call candidateReConnect ");
				// demo usage of candidateReConnect
				// discover service timeout happens a lot when samsung phones disconnected abnormally,
				// you can't connect to band by call candidateConnect(), it always stuck here, then call 
				//candidateReConnect can fix this issue.
				if(bt_device != null){
					tgBleManager.candidateReConnect(bt_device);
				}
				break;
			 /**
		     * BluetoothGattCallback.onConnectionStateChange() get disconnect message by accident
		     */
			case TGBleManager.STATE_CONNECTION_LOST:
				// do reconnect actions here
				break;

			}
			
		}
		
		
		private void handleBondResult(TGbondResult result) {
			// TODO Auto-generated method stub
			switch(result){
			case TGbondResultTokenAccepted: //Bond Success
				break;
			case TGbondResultError: // Unexpected Event, please contact NeuroSky Support
				break;
			case TGbondResultErrorBondedNoMatch: //Token has not been received by band. Retry.
				break;
			case TGbondResultErrorBadTokenFormat: // Bad token. Please check.
				break;
			case TGbondResultErrorTimeOut: // Bond timeout. Please check connection. Retry
				break;
			case TGbondResultErrorNoConnection: // Reconnect. Retry.
				break;
			case TGbondResultErrorReadTimeOut: // Retry
				break;
			case TGbondResultErrorWriteFail: // Retry
				break;
			case TGbondResultErrorTargetIsAlreadyBonded: //Reset or bond to another band.
				break;
			case TGbondAppErrorNoPotentialBondDelegate: // not used
				break;
			case TGbondResultTokenReleased: //Token been erase from app. releaseBond() called.
				break;
			case TGbondResultErrorUnSupportedHardware: //Check HW / FW version
				break;
			case TGbondResultErrorTargetHasWrongSN: //Possible bond to undesired band.
				break;
			default:
				break;
				
			}
		}


		public void connectToDevice(){
			Log.d(TAG,"connectToDevice called by"+this);
			
			// release the resource before connect
			tgBleManager.disconnect();
			tgBleManager.close();
			
			setUpDeviceListView();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check 
                if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("This app needs location access");
                    builder.setMessage("Please grant location access so this app can detect beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    });
                    builder.show();
                }
				tgBleManager.startScan();
            }
            else{
                tgBleManager.startScan();
            }

		}

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    tgBleManager.startScan();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    private void setUpDeviceListView(){
			Log.d(TAG,"setUpDeviceListView called by"+this);
	    	LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.dialog_select_device, null);
			list_select = (ListView) view.findViewById(R.id.list_select);
			//selectDialog = new Dialog(this, R.style.dialog1);
			selectDialog = new Dialog(this);
			selectDialog.setTitle("Select Device");
			selectDialog.setContentView(view);
	    	//List device dialog

	    	deviceListApapter = new BTDeviceListAdapter(this);
	    	list_select.setAdapter(deviceListApapter);
	    	list_select.setOnItemClickListener(selectDeviceItemClickListener);
	    	
	    	selectDialog.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					tgBleManager.stopScan();
					Log.e(TAG,"onCancel called!");

				}
	    	});
	    	selectDialog.show();

	    }

	//Select device operation
	private OnItemClickListener selectDeviceItemClickListener = new OnItemClickListener(){


		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Rico ####  list_select onItemClick     ");
			tgBleManager.stopScan();
			
	    	mBluetoothDevice =deviceListApapter.getDevice(arg2);
	    	Log.d(TAG,deviceListApapter.getRecord(arg2).toString());
	    	byte[] bytes = deviceListApapter.getRecord(arg2);
	    	
	    	//Get the Device name, for some android phone, you can't get the name by mBluetoothDevice.getName()
	    	// so we have to get the name from BLE advertisement
	    	StringBuffer btName = new StringBuffer();
	    	for(int i = 2;i<bytes[0] +1;i++){
	    		btName.append((char)bytes[i]);
	    	}
	    	Log.d(TAG,"Device Name:" +btName.toString() );
	    	selectDialog.dismiss();
	    	selectDialog = null;
	    	
	    	//save device address
	    	//spEditor.putString(DEVICE_ADDRESS,mBluetoothDevice.getAddress());
	    	//spEditor.commit();
			
			tgBleManager.candidateConnect(mBluetoothDevice);
			showToast("connecting...");
		}

	};
	
	private void showToast(final String msg ){
		handler.post(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	
	public TGBleManager getTGBleManager(){
		return tgBleManager;
	}
	
	public SeagullDevice getDevice(){
		return seagullDevice;
	}
	
	public SharedPreferences getSharedPreferences(){
		return sharePreferences;
	}
	
	public SharedPreferences.Editor getSharedPreferencesEditor(){
		return shareEditor;
	}
	
	public void setBondCallback(ICallbackForBond callback){
		this.callback = callback;
	}
}
