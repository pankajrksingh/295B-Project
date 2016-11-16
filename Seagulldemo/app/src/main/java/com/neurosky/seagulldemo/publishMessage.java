package com.neurosky.seagulldemo;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.neurosky.seagulldemo.BaseActivity.client;

/**
 * Created by Pankaj on 11/6/2016.
 */

//public class publishMessage extends Thread {
//    public String msg;
//
//    publishMessage(String inputmsg)
//    {
//        this.msg = inputmsg;
//    }
//
//    @Override
//    public void run(){
//        byte[] encodedPayload = new byte[0];
//        try {
//            encodedPayload = msg.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        MqttMessage message = new MqttMessage(encodedPayload);
//        try {
//            client.publish("pankaj123", message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    };
//}


public class publishMessage{
    public void sendMessage(final String inputmsg)
    {
        Runnable runnable = new Runnable() {
            final String msg = inputmsg;
            @Override
            public void run() {
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = msg.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                MqttMessage message = new MqttMessage(encodedPayload);
                try {
                    client.publish("pankaj123", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }
}