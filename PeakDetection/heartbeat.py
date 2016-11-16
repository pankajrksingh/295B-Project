import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import math
from scipy.interpolate import interp1d #Import the interpolate function from SciPy
from scipy.signal import butter, lfilter #Import the extra module required
from scipy import signal

measures = {}


def get_data(filename):
    dataset = pd.read_csv(filename)
    return dataset

def get_smple_rate(dataset, time_type):
    # Simple way to get sample rate
    if time_type == 'timer':
        sampletimer = [x for x in dataset.timer]  # dataset.timer is a ms counter with start of recording at '0'
        print("len(sampletimer) : " + str(len(sampletimer)))
        print("sampletimer[-1] : " + str(sampletimer[-1]))
        # measures['fs'] = math.ceil((len(sampletimer) / sampletimer[-1]) * 100)  # Divide total length of dataset by last timer entry. This is in ms, so multiply by 1000 to get Hz value
        measures['fs'] = math.ceil((float(len(sampletimer)) / float(sampletimer[-1])) * 100)

    # If your timer is a date time string, convert to UNIX timestamp to more easily calculate with, use something like this:
    if time_type == 'datetime':
        unix_time = []
        for x in dataset.datetime:
            dt = datetime.datetime.strptime(Datum, "%Y-%m-%d %H:%M:%S.%f")
            unix_time.append(time.mktime(dt.timetuple()) + (dt.microsecond / 1000000.0))
        measures['fs'] = (len(unix_time) / (unix_time[-1] - unix_time[0]))

    print ("Sample Rate : " + str(measures['fs']))
    return measures['fs']


# Define the filter
def butter_lowpass(cutoff, fs, order=5):
    nyq = 0.5 * fs  # Nyquist frequeny is half the sampling frequency
    normal_cutoff = cutoff / nyq
    b, a = butter(order, normal_cutoff, btype='low', analog=False)
    return b, a


def butter_lowpass_filter(data, cutoff, fs, order):
    b, a = butter_lowpass(cutoff, fs, order=order)
    y = lfilter(b, a, data)
    return y

def filter_signal(dataset):
    #Filtering the signals
    filtered = butter_lowpass_filter(dataset.hart, 2.5, 100.0, 5)

    # Plot it
    plt.subplot(211)
    plt.plot(dataset.hart, color='Blue', alpha=0.5, label='Original Signal')
    plt.legend(loc=4)
    plt.subplot(212)
    plt.plot(filtered, color='Red', label='Filtered Signal')
    # plt.ylim(200,
    #          800)  # limit filtered signal to have same y-axis as original (filter response starts at 0 so otherwise the plot will be scaled)
    plt.legend(loc=4)
    plt.show()
    return filtered


def rolmean(dataset, hrw, fs):
    mov_avg = pd.rolling_mean(dataset.hart, window=(hrw * fs))
    avg_hr = (np.mean(dataset.hart))
    mov_avg = [avg_hr if math.isnan(x) else x for x in mov_avg]
    # mov_avg = [x * 1.2 for x in mov_avg]
    dataset['hart_rollingmean'] = mov_avg


def detect_peaks(dataset, ma_perc, fs):  # Change the function to accept a moving average percentage 'ma_perc' argument
    rolmean = [(x + ((x / 100) * ma_perc)) for x in
               dataset.hart_rollingmean]  # Raise moving average with passed ma_perc
    window = []
    peaklist = []
    listpos = 0
    for datapoint in dataset.hart:
        rollingmean = rolmean[listpos]
        if (datapoint <= rollingmean) and (len(window) <= 1):  # Here is the update in (datapoint <= rollingmean)
            listpos += 1
        elif (datapoint > rollingmean):
            window.append(datapoint)
            listpos += 1
        else:
            maximum = max(window)
            beatposition = listpos - len(window) + (window.index(max(window)))
            peaklist.append(beatposition)
            window = []
            listpos += 1
    measures['peaklist'] = peaklist
    measures['ybeat'] = [dataset.hart[x] for x in peaklist]
    measures['rolmean'] = rolmean
    calc_RR(dataset, fs)
    measures['rrsd'] = np.std(measures['RR_list'])


def fit_peaks(dataset, fs):
    ma_perc_list = [5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 150, 200,
                    300]  # List with moving average raise percentages, make as detailed as you like but keep an eye on speed
    rrsd = []
    valid_ma = []
    for x in ma_perc_list:  # Detect peaks with all percentages, append results to list 'rrsd'
        detect_peaks(dataset, x, fs)
        bpm = ((len(measures['peaklist']) / (len(dataset.hart) / fs)) * 60)
        rrsd.append([measures['rrsd'], bpm, x])

    for x, y, z in rrsd:  # Test list entries and select valid measures
        if ((x > 1) and ((y > 30) and (y < 130))):
            valid_ma.append([x, z])

    measures['best'] = min(valid_ma, key=lambda t: t[0])[
        1]  # Save the ma_perc for plotting purposes later on (not needed)
    detect_peaks(dataset, min(valid_ma, key=lambda t: t[0])[1],
                 fs)  # Detect peaks with 'ma_perc' that goes with lowest rrsd

def calc_RR(dataset, fs):
    peaklist = measures['peaklist']
    RR_list = []
    cnt = 0
    while (cnt < (len(peaklist) - 1)):
        RR_interval = (peaklist[cnt + 1] - peaklist[cnt])
        ms_dist = ((RR_interval / fs) * 1000.0)
        RR_list.append(ms_dist)
        cnt += 1

    RR_diff = []
    RR_sqdiff = []
    cnt = 0
    while (cnt < (len(RR_list) - 1)):
        RR_diff.append(abs(RR_list[cnt] - RR_list[cnt + 1]))
        RR_sqdiff.append(math.pow(RR_list[cnt] - RR_list[cnt + 1], 2))
        cnt += 1
    measures['RR_list'] = RR_list
    measures['RR_diff'] = RR_diff
    measures['RR_sqdiff'] = RR_sqdiff


def calc_ts_measures():
    RR_list = measures['RR_list']
    RR_diff = measures['RR_diff']
    RR_sqdiff = measures['RR_sqdiff']
    measures['bpm'] = 60000 / np.mean(RR_list)
    measures['ibi'] = np.mean(RR_list)
    measures['sdnn'] = np.std(RR_list)
    measures['sdsd'] = np.std(RR_diff)
    measures['rmssd'] = np.sqrt(np.mean(RR_sqdiff))
    NN20 = [x for x in RR_diff if (x > 20)]
    NN50 = [x for x in RR_diff if (x > 50)]
    measures['nn20'] = NN20
    measures['nn50'] = NN50
    measures['pnn20'] = float(len(NN20)) / float(len(RR_diff))
    measures['pnn50'] = float(len(NN50)) / float(len(RR_diff))

def calc_fd_measures(dataset, fs):
    peaklist = measures['peaklist']  # First retrieve the lists we need
    RR_list = measures['RR_list']

    RR_x = peaklist[1:]  # Remove the first entry, because first interval is assigned to the second beat.
    RR_y = RR_list  # Y-values are equal to interval lengths

    RR_x_new = np.linspace(RR_x[0], RR_x[-1], RR_x[
        -1])  # Create evenly spaced timeline starting at the second peak, its endpoint and length equal to position of last peak

    f = interp1d(RR_x, RR_y, kind='cubic')  # Interpolate the signal with cubic spline interpolation

    print("Frequency domain at x 250")
    print f(250)

    plt.title("Original and Interpolated Signal")
    plt.plot(RR_x, RR_y, label="Original", color='blue')
    plt.plot(RR_x_new, f(RR_x_new), label="Interpolated", color='red')
    plt.legend()
    plt.show()

    #Set variables
    n = len(dataset.hart) #Length of the signal
    fs = float(fs)
    frq = np.fft.fftfreq(len(dataset.hart), d=((1/fs))) #divide the bins into frequency categories
    frq = frq[range(n/2)] #Get single side of the frequency range

    # Do FFT
    Y = np.fft.fft(f(RR_x_new)) / n  # Calculate FFT
    Y = Y[range(n / 2)]  # Return one side of the FFT

    lf = np.trapz(abs(Y[(frq >= 0.04) & (
    frq <= 0.15)]))  # Slice frequency spectrum where x is between 0.04 and 0.15Hz (LF), and use NumPy's trapezoidal integration function to find the area
    print "LF:", lf

    hf = np.trapz(abs(Y[(frq >= 0.16) & (frq <= 0.5)]))  # Do the same for 0.16-0.5Hz (HF)
    print "HF:", hf

    # Plot
    plt.title("Frequency Spectrum of Heart Rate Variability")
    plt.xlim(0, 0.6)  # Limit X axis to frequencies of interest (0-0.6Hz for visibility, we are interested in 0.04-0.5)
    plt.ylim(0, 50)  # Limit Y axis for visibility
    plt.plot(frq, abs(Y))  # Plot it
    plt.xlabel("Frequencies in Hz")
    plt.show()

def plotter(dataset, title):
    peaklist = measures['peaklist']
    ybeat = measures['ybeat']
    plt.title(title)
    plt.plot(dataset.hart, alpha=0.5, color='blue', label="raw signal")
    plt.plot(dataset.hart_rollingmean, color='green', label="moving average")
    plt.scatter(peaklist, ybeat, color='red', label="average: %.1f BPM" % measures['bpm'])
    plt.scatter(peaklist, ybeat, color='yellow', label="RRSD: %.1f BPM" % measures['rrsd'])
    plt.legend(loc=4, framealpha=0.6)
    plt.show()

def detect_p_wave(dataset):
    print "P Wave detection"
    # measures['pwavelist'] = []
    # measures['qwavelist'] = []
    qwavelist = []
    # measures['pbeat'] = []
    # measures['qbeat'] = []
    qbeat = []
    for index in range(len(measures['peaklist'])):
        index_search = measures['peaklist'][index]
        # print index_search
        # print (dataset.hart[index_search] - dataset.hart[index_search - 1])
        # index_search -= 1
        while ((dataset.hart[index_search] - dataset.hart[index_search-1]) > 0.05):
            # print index_search
            # print (dataset.hart[index_search] - dataset.hart[index_search-1])
            index_search -= 1

        qbeat.append(dataset.hart[index_search])
        qwavelist.append(index_search)

    measures['qwavelist'] = qwavelist
    measures['qbeat'] = qbeat

    print (measures['peaklist'])
    print (measures['qbeat'])
    print (measures['qwavelist'])




def false_detect_signals(dataset):
    RR_list = measures['RR_list']  # Get measures
    peaklist = measures['peaklist']
    ybeat = measures['ybeat']

    print ("np.mean(RR_list) : " + str(np.mean(RR_list)))

    upper_threshold = (np.mean(RR_list) + 300)  # Set thresholds
    lower_threshold = (np.mean(RR_list) - 300)

    # detect outliers
    cnt = 0
    removed_beats = []
    removed_beats_y = []
    RR2 = []
    while cnt < len(RR_list):
        if (RR_list[cnt] < upper_threshold) and (RR_list[cnt] > lower_threshold):
            RR2.append(RR_list[cnt])
            cnt += 1
        else:
            removed_beats.append(peaklist[cnt])
            removed_beats_y.append(ybeat[cnt])
            cnt += 1

    measures['RR_list_cor'] = RR2  # Append corrected RR-list to dictionary

    detect_p_wave(dataset)

    plt.subplot(211)
    plt.title('Marked Uncertain Peaks')
    plt.plot(dataset.hart, color='blue', alpha=0.6, label='heart rate signal')
    plt.plot(measures['rolmean'], color='green')
    plt.scatter(measures['peaklist'], measures['ybeat'], color='green')
    plt.scatter(removed_beats, removed_beats_y, color='red', label='Detection uncertain')
    plt.scatter(measures['qwavelist'], measures['qbeat'], color='yellow')
    # plt.xlim(8000, 10000)
    # plt.ylim(350, 650)
    plt.legend(framealpha=0.6, loc=4)

    plt.subplot(212)
    plt.title("RR-intervals with thresholds")
    plt.plot(RR_list)
    plt.axhline(y=upper_threshold, color='red')
    plt.axhline(y=lower_threshold, color='red')
    plt.show()

# def process(dataset, hrw,
#             fs):  # Remember; hrw was the one-sided window size (we used 0.75) and fs was the sample rate (file is recorded at 100Hz)
#     rolmean(dataset, hrw, fs)
#     detect_peaks(dataset)
#     calc_RR(dataset, fs)
#     calc_bpm()
#     plotter(dataset, "My Heartbeat Plot")

#Don't forget to update our process() wrapper to include the new function
def process(input_dataset, hrw, fs):
    # fs = get_smple_rate(input_dataset, 'timer')
    fs = 100.0
    d = {'hart': filter_signal(input_dataset)}
    dataset = pd.DataFrame(data=d)
    rolmean(dataset, hrw, fs)
    fit_peaks(dataset, fs)
    calc_ts_measures()
    calc_fd_measures(dataset, fs)
    plotter(dataset, "My Heartbeat Plot")
    false_detect_signals(dataset)


    # data = filter_signal(input_dataset)
    # val = signal.find_peaks_cwt(data, np.arange(1, 4))
    # print val
    # # val = np.array(val) - 1
    #
    # peak_val = []
    #
    # for i in val:
    #     peak_val.append(data[i])
    #
    #
    # print "-------------------------------"
    # print val
    # print "-------------------------------"
    # print peak_val
    # plt.title('All Peaks')
    # plt.plot(data, color='blue', alpha=0.6, label='heart rate signal')
    # plt.scatter(val, peak_val, color='green')
    # plt.xlim(2000, 8000)
    # plt.show()
    #
    # print "Done"
