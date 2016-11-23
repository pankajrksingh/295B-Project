import heartbeat as hb #Assuming we named the file 'heartbeat.py'
import heartbeat1 as hb1 #Assuming we named the file 'heartbeat1.py'
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
import paho.mqtt.client as mqtt
import numpy as np

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




# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("pankaj123")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    input_array = msg.payload.split(',')
    # print(msg.topic+" "+str(msg.payload))
    temp_array = np.array(input_array, dtype='|S4')
    dataset = temp_array.astype(np.int)
    # print dataset
    hb.process(dataset, 0.75, 100)


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("54.244.148.72", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()