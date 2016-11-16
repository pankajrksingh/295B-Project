package com.neurosky.seagulldemo;

/**
 * Created by Pankaj on 11/7/2016.
 */

import org.apache.commons.math.*;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static com.neurosky.seagulldemo.BaseActivity.client;

//import java.util.ArrayList;

public class HearBeatCount{

    private double ecgData[];
    private int heartBeatCount = 0;
    Mean mean = new Mean();
    long exe_time = 0;
    StandardDeviation standardDeviation = new StandardDeviation();
//    ArrayList<Double> heartBeatValues = new ArrayList<Double>();

    public HearBeatCount(double rawEcgData[], long time_temp)
    {
        this.ecgData = rawEcgData;
        this.exe_time = time_temp;
    }

    public void calculateHeartBeat()
    {
        double ecgMean = mean.evaluate(ecgData);
        double ecgStandardDeviation = standardDeviation.evaluate(ecgData);
        double normalDist = ecgMean + (2 * ecgStandardDeviation);
        JSONObject json = new JSONObject();
        try {
            json.put("HEART_BEAT_EXECUTION_TIME", exe_time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        double tempmax = 0;
        int  tempindex = 0;

        for(int i=0; i<ecgData.length; i++)
        {
            if(ecgData[i] > normalDist)
            {
                if(Math.abs(i - tempindex) < 10)
                {
//                    tempmax = ecgData[i];
                    tempindex = i;
                    //do something
                }
                else
                {
//                    tempmax = ecgData[i];
                    tempindex = i;
                    heartBeatCount++;
//                    heartBeatValues.add(ecgData[i]);
                }
            }
        }
//        return heartBeatCount;

        try {
            json.put("MSG_HEART_BEAT_COUNT",heartBeatCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        publishMessage hbpublish = new publishMessage();
        hbpublish.sendMessage(json.toString());

    }

}
