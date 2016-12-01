package com.neurosky.seagulldemo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.TimeZone;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.neurosky.blecommunication.SeagullDevice;
import com.neurosky.blecommunication.TGBleManager;
import com.neurosky.blecommunication.base.SeagullEnumInfo.TGecgResult;
import com.neurosky.blecommunication.base.TGBleHandler;



public class RealtimeECGActivity extends BaseActivity {
	//	private static final String TAG = RealtimeECGActivity.class.getSimpleName();
	private static final String TAG = "Algo3p0";
	private TGBleManager tgBleManager = null;
	private SeagullDevice device = null;
	private LinearLayout wave_layout;
	private Button btn_start = null;
	private Button btn_stop = null;

	private TextView tv_heartRate, tv_rrInterval, tv_robust;
	// private TextView tv_respiratoryRate;
	private TextView tv_heartBeatCount, tv_hrv;
	private TextView tv_mood, tv_heartAge, tv_stress;
	private TextView tv_ssq, tv_lsq, tv_finalHRV;

	/****************** Algo SDK 3.0 variable define *********************/
	private Handler mHandler;
	private int currentActiveAlgo = 0;
	private boolean bInited = false;
	private boolean bRunning = false;
	private int activeProfile = -1;

	private int hr = -1;
	private int robust = -1;
	private int mood = -1;
	private int r2r = -1;
	private int hrv = -1;
	private int heartage = -1;
	private int afib = -1;
	private int rdetected = -1;
	private int stress = -1;
	private int heartbeat = -1;
	//hardcode of sampling, needs to be changed later
//	private int sample_count = 15360;
	private int sample_count = 3840;
//	private double ecgSampleValueList[] = new double[sample_count];
//	private JSONObject jsonArray = new JSONObject();
	private JSONArray ecgSampleValueArray = new JSONArray();
	private JSONArray ecgTimeStampArray = new JSONArray();
	private JSONObject ecgData = new JSONObject();
	private int counter = 0;

//	public MqttClient client = null;
    public ArrayList<Integer> ecgValues;
//	publishMessage msgPublish = new publishMessage();

	private static Calendar calendar = null;
	private long startTime = 0;
	private long endTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		/****************** init UI  *********************/
		setContentView(R.layout.realtime_ecg_view);

		initUI();
		setUpDrawWaveView();

		/****************** init Algo ********************/
		mHandler = new Handler();
		resetECG(); // algo reset
		setAlgoListener();

		btn_start.setText("Start");
		btn_start.setEnabled(true);


//		try {
//			//client = new MqttClient("tcp://broker.mqttdashboard.com:1883", MqttClient.generateClientId(), null);
//
//            client = new MqttClient("tcp://54.244.148.72:1883", MqttClient.generateClientId(), null);
//
//			client.connect();
//
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
	}


	private void initUI() {
		// TODO Auto-generated method stub
		tv_heartRate = (TextView) findViewById(R.id.tv_heartRate);
		tv_heartRate.setText("");
		tv_rrInterval = (TextView) findViewById(R.id.tv_rrInterval);
		tv_rrInterval.setText("");
		tv_robust = (TextView) findViewById(R.id.tv_robust);
		tv_robust.setText("");

		// RespiratoryRate, HeaertBeatCount and HRV display
		// tv_respiratoryRate = (TextView)
		// findViewById(R.id.tv_respiratoryRate);
		// tv_respiratoryRate.setText("");
		tv_heartBeatCount = (TextView) findViewById(R.id.tv_heartBeatCount);
		tv_heartBeatCount.setText("");
		tv_hrv = (TextView) findViewById(R.id.tv_hrv);
		tv_hrv.setText("");

		// Mood, heartAge and Stress display
		tv_mood = (TextView) findViewById(R.id.tv_mood);
		tv_mood.setText("");
		tv_heartAge = (TextView) findViewById(R.id.tv_heartAge);
		tv_heartAge.setText("");
		tv_stress = (TextView) findViewById(R.id.tv_stress);
		tv_stress.setText("");

		// signal quality and FinalHRV display
		tv_ssq = (TextView) findViewById(R.id.tv_ssq);
		tv_ssq.setText("");
		tv_lsq = (TextView) findViewById(R.id.tv_lsq);
		tv_lsq.setText("");
		tv_finalHRV = (TextView) findViewById(R.id.tv_finalHRV);
		tv_finalHRV.setText("");

		wave_layout = (LinearLayout) findViewById(R.id.wave_layout);
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		setListener();


        ecgValues = new ArrayList<Integer>();
	}

	private void setAlgoListener() {
		// TODO: remove NskAlgoSdk listener for Algo SDK		
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				resetUI();
				if (device != null && device.isBond()) {
					device.startRealTimeECG();
				} else {

					setBondCallback(new ICallbackForBond() {
						@Override
						public void callFunction() {
							// TODO Auto-generated method stub
							device.startRealTimeECG();
						}
					});
					connectToDevice();
				}
			}

		});

		btn_stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				device.stopRealTimeECG();
//				nskAlgoSdk.NskAlgoStop();


				btn_start.setText("Start");

			}

		});
	}



    private ArrayList<Integer> parseDataForHB(ArrayList<Integer> ecgValues, int averageHeartBeatValue)
    {
        int previousVal = 0;
        ArrayList<Integer> heartBeatValues = new ArrayList<>();
        Iterator it = ecgValues.iterator();


        while(it.hasNext())
        {
            int ecgVal = (Integer)it.next();
            if(ecgVal >= (averageHeartBeatValue * .5))
            {
                if(((ecgVal > 0) && (previousVal < 0)) ||
                        ((ecgVal < 0) && (previousVal > 0)))
                {
                    heartBeatValues.add(ecgVal);
                    averageHeartBeatValue = (int) mean(heartBeatValues);
                }
            }
            previousVal = ecgVal;
        }
        return  heartBeatValues;
    }

    private double mean(ArrayList<Integer> heartBeatValues) {

        double sum = 0;
        for (int i = 0; i < heartBeatValues.size(); i++) {
            sum += heartBeatValues.get(i);
        }
        return sum / heartBeatValues.size();

    }


    private void resetUI() {
		tv_heartRate.setText("");
		tv_rrInterval.setText("");
		tv_robust.setText("");
		// tv_respiratoryRate.setText("");
		tv_heartBeatCount.setText("");
		tv_hrv.setText("");
		tv_mood.setText("");
		tv_heartAge.setText("");
		tv_stress.setText("");
		tv_ssq.setText("");
		tv_lsq.setText("");
		tv_finalHRV.setText("");
		waveView.clear();
		dataCount = 0;
	}

	private int dataCount = 0;

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
		tgBleManager = TGBleManager.getInstance();
		device = (SeagullDevice) tgBleManager.getDevice();

		tgBleManager.setHandler(msgHandler);
		// device.setTGBleHandler(callback);
		device.setTGBleHandler(null);// this means we only use handle to
		// translate data

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

	private Handler msgHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case SeagullDevice.MSG_REALTIME_ECG:
					short pqValue[] = {(short) 200};
					try {
						handleRealtimeECG(msg.arg1, msg.arg2, msg.obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case SeagullDevice.MSG_BATTERY_LEVEL:
					Log.d(TAG, "Battery info: " + msg.arg1);
					break;

			}
			super.handleMessage(msg);
		}

	};

	private TGBleHandler callback = new TGBleHandler() {

		/**
		 * MSG_HISTORY_ECG, MSG_HISTORY_ECG_SAMPLE,int<br/>
		 *
		 *
		 */

		@Override
		public void onDataReceived(int what, int arg1, int arg2, Object obj) {
			// TODO Auto-generated method stub
			switch (what) {
				// use TGBleHandler to receive data will be faster than Handler
				// If you want to receive ecg data with Handler, please call
				// device.setTGBleHandler(null); in onResume()
				case SeagullDevice.MSG_REALTIME_ECG:
					try {
						handleRealtimeECG(arg1, arg2, obj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
			}
		}
	};

	protected void handleRealtimeECG(int arg1, int arg2, Object obj) throws JSONException {
		// TODO Auto-generated method stub
		switch (arg1) {
			// If you want to receive ecg data with TGBleHandler , please call	device.setTGBleHandler(callback) in onResume()
			case SeagullDevice.MSG_REALTIME_ECG_SAMPLE:
				//Sending ecg data to mqtt
//				JSONObject json = new JSONObject();
//				json.put("MSG_REALTIME_ECG_SAMPLE",arg2);
////				new publishMessage(json.toString()).start();
//				publishMessage ecgPubish = new publishMessage();
//				ecgPubish.sendMessage(json.toString());

				//Calculate HeartBeat
//				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&& counter : " + counter + "  &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				if(counter == 0)
				{
					startTime = System.currentTimeMillis();
				}

				if(counter == sample_count-1)
				{
					System.out.println("&&&&&&&&&&&&&&&&&&&&&&&& counter : " + counter + "  &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//					ecgSampleValueList[counter] = ((double) arg2);
					endTime = System.currentTimeMillis();
					long  execution_time = (endTime - startTime);
					ecgSampleValueArray.put((double) arg2);
					ecgTimeStampArray.put(execution_time);

					ecgData.accumulate("hart", ecgSampleValueArray);
					ecgData.accumulate("timer",ecgTimeStampArray);

//					HearBeatCount hb = new HearBeatCount(ecgSampleValueList, execution_time);
//					hb.calculateHeartBeat();
					publishMessage hbpublish = new publishMessage();
					hbpublish.sendMessage(ecgData.toString(), "pankaj123");

					counter = 0;
					System.out.println("##################################################################");
					System.out.println("##################################################################");
					System.out.println("##################################################################");
					System.out.println("ecgSampleValueArray : " + ecgSampleValueArray.toString());
					System.out.println("ecgTimeStampArray : " + ecgTimeStampArray.toString());
					System.out.println("ecgData : " + ecgData.toString());
					System.out.println("##################################################################");
					System.out.println("##################################################################");
					System.out.println("##################################################################");

					ecgSampleValueArray = new JSONArray();
					ecgTimeStampArray = new JSONArray();
					ecgData = new JSONObject();
				}
				else
				{
//					JSONArray temp_json = new JSONObject();
//					jsonArray.accumulate()
					endTime = System.currentTimeMillis();
					long  execution_time = (endTime - startTime);
//					ecgSampleValueList[counter++] = ((double) arg2);
					ecgSampleValueArray.put((double) arg2);
//					ecgTimeStampArray.put(execution_time);
					counter++;
				}


				//Plotting Graph
				updateWaveView(arg2);
//				Log.i("Test", "arg2="+arg2);
				// send the data to algorithm sdk here
//				if (arg1 == SeagullDevice.MSG_REALTIME_ECG_SAMPLE) {
//					// send the  data to algorithm  sdk here arg2 is the data put the data into algorithm sdk here.
//					short ecg_data[] = { (short) arg2 };
//
//					JSONObject json = new JSONObject();
//					json.put("MSG_REALTIME_ECG_SAMPLE",arg2);
//					new publishMessage(json.toString()).start();



//                    String topic = "pankaj123";
//                    JSONObject json = new JSONObject();
//                    try {
//                        json.put("MSG_REALTIME_ECG_SAMPLE",arg2);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    String payload = json.toString();
//					byte[] encodedPayload = new byte[0];
//					try {
//						encodedPayload = payload.getBytes("UTF-8");
//						MqttMessage message = new MqttMessage(encodedPayload);
//						//message.setRetained(true);
//						client.publish(topic, message);
//                        ecgValues.add(arg2);
//
//                        if(diffInMinutes>=1 && diffInSeconds >=0 )
//                        {
//                            ArrayList<Integer> heartBeat = parseDataForHB(ecgValues,75);
//
//                            try {
//                                json.put("MSG_REALTIME_HEARTBEAT",heartBeat);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            payload = json.toString();
//                            encodedPayload = new byte[0];
//                            try {
//                                encodedPayload = payload.getBytes("UTF-8");
//								message = new MqttMessage(encodedPayload);
//								//message.setRetained(true);
//								client.publish(topic, message);
//                            } catch (UnsupportedEncodingException | MqttException e) {
//                                e.printStackTrace();
//                            }
//                            diffInSeconds=0;
//                        }
//
//					} catch (UnsupportedEncodingException | MqttException e) {
//						e.printStackTrace();
//					}
////				Log.i(TAG, "raw:"+ecg_data[0]);
//					dataCount += 2;
//				}



				break;

			case SeagullDevice.MSG_REALTIME_ECG_COMMENT:

				Log.i(TAG, "MSG_REALTIME_ECG_COMMENT: " + obj);

//                String topic = "pankaj123";
//                JSONObject json = new JSONObject();
//                try {
//                    json.put("MSG_REALTIME_ECG_COMMENT",obj);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String payload = json.toString();
//                byte[] encodedPayload = new byte[0];
//                try {
//                    encodedPayload = payload.getBytes("UTF-8");
//                    MqttMessage message = new MqttMessage(encodedPayload);
//                    message.setRetained(true);
//                    client.publish(topic, message);
//                } catch (UnsupportedEncodingException | MqttException e) {
//                    e.printStackTrace();
//                }

				break;
			case SeagullDevice.MSG_REALTIME_ECG_FINALHR:

				Log.i(TAG, "MSG_REALTIME_ECG_FINALHR: " + arg2);
				break;
			case SeagullDevice.MSG_REALTIME_ECG_SAMPLERATE:
				Log.i(TAG, "MSG_REALTIME_ECG_SAMPLERATE: " + arg2);

				break;
			case SeagullDevice.MSG_REALTIME_ECG_STOP:
				Log.i(TAG, "MSG_REALTIME_ECG_STOP: " + obj);

				tv_lsq.post(new Runnable() {
					@Override
					public void run() {
						tv_lsq.setText(String.valueOf(robust));
					}
				});
				tv_finalHRV.post(new Runnable() {
					@Override
					public void run() {
						tv_finalHRV.setText(String.valueOf(hrv));
					}
				});

				tv_stress.post(new Runnable() { // you can also get the stress value
					// when you get 30 rr_interval
					@Override
					public void run() {
						tv_stress.setText(String.valueOf(stress));
					}
				});

				handleECGStopResult((TGecgResult) obj);
				break;
			case SeagullDevice.MSG_REALTIME_ECG_TS:
				Log.i(TAG, "MSG_REALTIME_ECG_TS: " + obj);

//                String topic1 = "pankaj123";
//                JSONObject json1 = new JSONObject();
//                try {
//                    json1.put("MSG_REALTIME_ECG_TS",obj);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String payload1 = json1.toString();
//                byte[] encodedPayload1 = new byte[0];
//                try {
//                    encodedPayload = payload1.getBytes("UTF-8");
//                    MqttMessage message = new MqttMessage(encodedPayload);
//                    message.setRetained(true);
//                    client.publish(topic1, message);
//                } catch (UnsupportedEncodingException | MqttException e) {
//                    e.printStackTrace();
//                }


				break;

		}
	}

	private void handleECGStopResult(TGecgResult obj) {
		// TODO Auto-generated method stub
		switch (obj) {
			case TGecgTerminatedNormally: // Operation terminate successfully
				break;
			case TGecgTerminatedNoData: // Didn't get any data at the beginning.
				// Please check sensor and finger, retry.
				break;
			case TGecgTerminatedDataStopped: // Can't get data for a while.Please
				// check sensor and finger, retry.
				break;
			case TGecgTerminatedLostConnection: // Lost connection. Please check
				// connection, retry.
				break;
			case TGecgTerminatedAbort: // Terminated by calling disconnect() or
				// close() , the task is interrupted
				break;
		}

	}

	DrawWaveView waveView = null;

	public void setUpDrawWaveView() {
		// TODO use self view to drawing ECG
		waveView = new DrawWaveView(getApplicationContext());
		wave_layout.addView(waveView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		waveView.setValue(2048, 4000, -4000);

	}

	public void updateWaveView(int data) {
		if (waveView != null) {
			waveView.updateData(data);
		}
	}

	private int sampleRate = 512;
	private int outputInterval = 30;
	private int outputPoint = 30;
	private int heartAgeRecordNumber = 5;
	private String name = "Mary";
	private int weight = 65;
	private int height = 170;
	private int age = 30;
	private boolean female = true;
	private String heartAgePath = "";
	private String path = "";
	private int stressFeedback = 0;
	private int respiratoryRateOutputInterval = 60;

	private void resetECG() {
		// TOD: remove for init NskAlgo and reset NskAlgo
	}

	public void showToast(final String msg, final int timeStyle) {
		RealtimeECGActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), msg, timeStyle).show();
			}

		});
	}


}
