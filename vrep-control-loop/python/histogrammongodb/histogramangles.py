import glob
from datetime import date

import pandas as pd
import matplotlib.pyplot as pyplot
import matplotlib
import argparse
import pymongo
import math
import numpy as np


from pandas import DataFrame



def main():
    print("histogram")
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    angle_collection = client.histograms.test

    cursor_chandigarh = angle_collection.find({"city": "Chandigarh"})
    entries_chandigarh = list(cursor_chandigarh)
    data_frame_chandigarh = pd.DataFrame(entries_chandigarh)
    dropped_frame_chandigarh = data_frame_chandigarh.drop(columns=['_id', 'city'])
    dropped_frame_chandigarh = dropped_frame_chandigarh.apply(lambda x: x / 10.0)
    print("should show dataframe head here")
    print(dropped_frame_chandigarh.head())
    print("now attemptimg to plot")
    histograms_chandigarh = dropped_frame_chandigarh.hist(bins=90, log=True)
    for array in histograms_chandigarh:
        for subplot in array:
            subplot.set_xlim((-18, 18))

    cursor_luebeck = angle_collection.find({"city": "Luebeck"})
    entries_luebeck = list(cursor_luebeck)
    data_frame_luebeck = pd.DataFrame(entries_luebeck)
    dropped_frame_luebeck = data_frame_luebeck.drop(columns=['_id', 'city'])
    dropped_frame_luebeck = dropped_frame_luebeck.apply(lambda x: x / 10.0)
    print("should show dataframe head here")
    print(dropped_frame_luebeck.head())
    print("now attemptimg to plot")
    histograms_luebeck = dropped_frame_luebeck.hist(bins=90, log=True)
    for array in histograms_luebeck:
        for subplot in array:
            subplot.set_xlim((-18, 18))

    pyplot.show()


if __name__ == '__main__':
    main()
