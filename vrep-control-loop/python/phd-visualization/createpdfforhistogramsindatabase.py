import glob

import matplotlib.pyplot as pyplot
import csv
import math
import numpy as np
import json
import pymongo
import pandas as pd

from matplotlib.patches import Circle
from matplotlib.patches import Arrow
from matplotlib.patches import Polygon

from pandas import DataFrame


def paint_function(content, ax, fig):
    return lambda idx: paint_panda_row(idx, len(content.index), content.iloc[idx], ax, fig)


def paint_panda_row(idx, len, data, ax, fig):
    print('painting')
    ax.cla()
    ax.autoscale(True)
    the_text = "{} of {}".format(idx, len)
    ax.set_title(the_text)
    segs = data[0]
    for cur_seg in segs:
        ax.add_patch(cur_seg)
    fig.canvas.draw()


def main():
    print("bin content")
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    histogram_data = client.histograms.test1

    ## cursor_chandigarh = angle_collection.find({"city": "Chandigarh"})
    cursor_histogram = histogram_data.find()
    entries_histogram = list(cursor_histogram)
    data_in = []
    nr_of_clusters = 0
    for entry in entries_histogram:
        print("processed %s" % (nr_of_clusters))
        nr_of_clusters = nr_of_clusters + 1
        segs = []
        for trajectory in entry["trajectories"]:
            for vector in trajectory:
                bX = float(vector['bX'])
                bY = float(vector['bY'])
                dX = float(vector['dX'])
                dY = float(vector['dY'])
                segs.append(Arrow(bX, bY, dX, dY, width=1, linestyle="-", alpha=0.1, color='green'))
        data_in.append([segs, nr_of_clusters])

    all_content = DataFrame(data=data_in, columns=["segs", "idx"])
    figure = pyplot.figure()
    ax = figure.add_subplot(1, 1, 1)
    my_painter = paint_function(all_content, ax, figure)

    ax.autoscale(True)
    pyplot.gca().set_aspect("equal", adjustable="datalim")

    pyplot.show()


if __name__ == '__main__':
    main()
