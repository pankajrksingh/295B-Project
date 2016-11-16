import heartbeat as hb #Assuming we named the file 'heartbeat.py'

dataset = hb.get_data('clean_data.csv')
# dataset = hb.get_data('DIT_data.csv')
# dataset = hb.get_data('actual_data.csv')
hb.process(dataset, 0.75, 100)

#The module dict now contains all the variables computed over our signal:
# hb.measures['bpm']
# hb.measures['ibi']
# hb.measures['sdnn']
#etcetera

#Remember that you can get a list of all dictionary entries with "keys()":
print hb.measures.keys()