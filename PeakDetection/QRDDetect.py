import heartbeat as hb #Assuming we named the file 'heartbeat.py'
import heartbeat1 as hb1 #Assuming we named the file 'heartbeat1.py'
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
import paho.mqtt.client as mqtt
import numpy as np
import threading
import json
import time

# dataset = hb.get_data('data.csv')
# print dataset.hart
# dataset1 = hb1.get_data('actual_data.csv')
# hb.process(dataset, 0.75, 100)
# hb1.process(dataset1, 0.75, 100)

#The module dict now contains all the variables computed over our signal:
# hb.measures['bpm']
# hb.measures['ibi']
# hb.measures['sdnn']
#etcetera

#Remember that you can get a list of all dictionary entries with "keys()":
# print hb.measures.keys()


class MQTTThread(threading.Thread):
    def __init__(self, host, port):
        super(MQTTThread, self).__init__()
        self.host = host
        self.port = port
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message

    # The callback for when the client receives a CONNACK response from the server.
    def on_connect(self, client, userdata, flags, rc):
        print("Connected with result code "+str(rc))

        # Subscribing in on_connect() means that if we lose the connection and
        # reconnect then subscriptions will be renewed.
        print self.topic
        client.subscribe(self.topic)


    def subscribe_topic(self, topic):
        self.topic = topic
        self.client.subscribe(self.topic)


    # The callback for when a PUBLISH message is received from the server.
    def on_message(self, client, userdata, msg):
        print msg.payload
        # input_array = msg.payload.split(',')
        # print(msg.topic+" "+str(msg.payload))

    def publish_message(self, topic, msg):
        self.client.publish(topic, msg)

    def run(self):
        print "Connectinf to mqtt"
        self.client.connect(self.host, self.port, 60)
        self.client.loop_start()


temp = MQTTThread("54.244.148.72", 1883)
temp.subscribe_topic("testpankaj")
temp.run()
print "after thread"
i = 0

while True:
    # print "pankaj"

    temp.publish_message("testpankaj", i)
    i += 1
    time.sleep(1)