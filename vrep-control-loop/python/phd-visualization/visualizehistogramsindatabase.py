import glob

import matplotlib.pyplot as pyplot
import csv
import math
import numpy as np
import json
import pymongo
import pandas as pd

from matplotlib.patches import Arrow

from pandas import DataFrame


class ZoomPanAnimate:
    def __init__(self):
        self.press = None
        self.cur_xlim = None
        self.cur_ylim = None
        self.x0 = None
        self.y0 = None
        self.x1 = None
        self.y1 = None
        self.xpress = None
        self.ypress = None
        self.frame_idx = 0

    def animate_factory(self, ax, max_idx, paint_function):
        def on_keyboard(event):
            if event.key == "right":
                self.frame_idx = (self.frame_idx + 1) if self.frame_idx < max_idx else max_idx
            elif event.key == "left":
                self.frame_idx = (self.frame_idx - 1)  if (self.frame_idx > 0) else 0
            ax.clear()
            paint_function(self.frame_idx) # callback that does the actual drawing

        fig = ax.get_figure() # get the figure of interest

        # attach the call back
        fig.canvas.mpl_connect('key_press_event', on_keyboard)

        #return the function
        return on_keyboard

    def zoom_factory(self, ax, base_scale = 2.):
        def zoom(event):
            cur_xlim = ax.get_xlim()
            cur_ylim = ax.get_ylim()

            xdata = event.xdata # get event x location
            ydata = event.ydata # get event y location

            if event.button == 'down':
                # deal with zoom in
                scale_factor = 1 / base_scale
            elif event.button == 'up':
                # deal with zoom out
                scale_factor = base_scale
            else:
                # deal with something that should never happen
                scale_factor = 1
                print(event.button)

            new_width = (cur_xlim[1] - cur_xlim[0]) * scale_factor
            new_height = (cur_ylim[1] - cur_ylim[0]) * scale_factor

            relx = (cur_xlim[1] - xdata)/(cur_xlim[1] - cur_xlim[0])
            rely = (cur_ylim[1] - ydata)/(cur_ylim[1] - cur_ylim[0])

            ax.set_xlim([xdata - new_width * (1-relx), xdata + new_width * (relx)])
            ax.set_ylim([ydata - new_height * (1-rely), ydata + new_height * (rely)])
            ax.figure.canvas.draw()

        fig = ax.get_figure() # get the figure of interest
        fig.canvas.mpl_connect('scroll_event', zoom)

        return zoom

    def pan_factory(self, ax):
        def onPress(event):
            if event.inaxes != ax: return
            self.cur_xlim = ax.get_xlim()
            self.cur_ylim = ax.get_ylim()
            self.press = self.x0, self.y0, event.xdata, event.ydata
            self.x0, self.y0, self.xpress, self.ypress = self.press

        def onRelease(event):
            self.press = None
            ax.figure.canvas.draw()

        def onMotion(event):
            if self.press is None: return
            if event.inaxes != ax: return
            dx = event.xdata - self.xpress
            dy = event.ydata - self.ypress
            self.cur_xlim -= dx
            self.cur_ylim -= dy
            ax.set_xlim(self.cur_xlim)
            ax.set_ylim(self.cur_ylim)

            ax.figure.canvas.draw()

        fig = ax.get_figure() # get the figure of interest

        # attach the call back
        fig.canvas.mpl_connect('button_press_event',onPress)
        fig.canvas.mpl_connect('button_release_event',onRelease)
        fig.canvas.mpl_connect('motion_notify_event',onMotion)

        #return the function
        return onMotion


def paint_function(content, ax, fig):
    return lambda idx: paint_panda_row(idx, len(content.index), content.iloc[idx], ax, fig)


def paint_panda_row(idx, len, data, ax, fig):
    ax.cla()
    # ax.autoscale(True)
    the_text = "{} of {}".format(idx, len)
    ax.set_title(the_text)
    segs = data[0]
    for cur_seg in segs:
        ax.add_patch(cur_seg)
    ax.set_xlim(-100, 100)
    ax.set_ylim(-100, 100)
    fig.canvas.draw()


def main():
    print("bin content")
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    histogram_data = client.histograms.test1

    cursor_histogram = histogram_data.find().limit(10)
    entries_histogram = list(cursor_histogram)
    data_in = []
    nr_of_clusters = 0
    for entry in entries_histogram:
        trajectories = entry["trajectories"]
        print("processed %s with %s trajetories" % (nr_of_clusters, len(trajectories)))
        nr_of_clusters = nr_of_clusters + 1
        segs = []
        for trajectory in trajectories:
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
    zoom_pan_animate = ZoomPanAnimate()
    my_painter = paint_function(all_content, ax, figure)

    animate = zoom_pan_animate.animate_factory(ax, nr_of_clusters, my_painter)
    figZoom = zoom_pan_animate.zoom_factory(ax, base_scale=1.1)
    figPan = zoom_pan_animate.pan_factory(ax)
    # ax.autoscale(True)
    pyplot.gca().set_aspect("equal", adjustable="datalim")

    pyplot.show()


if __name__ == '__main__':
    main()
