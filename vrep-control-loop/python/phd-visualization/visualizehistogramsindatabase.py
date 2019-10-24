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


def arc_patch(center, radius, theta1, theta2, dir, idx, resolution=50, **kwargs):
    # generate the points
    # print("theta1: {}, theta2: {}, dir: {}, idx: {}".format(theta1, theta2, dir, idx))
    theta = np.linspace(0, 0, resolution)
    if(dir=="left"):
        if(theta1 > 0 and theta2 < 0):
            theta2 = 2 * math.pi + theta2
        theta = np.linspace(theta1, theta2, resolution)
    elif (dir=="right"):
        if(theta1 < 0 and theta2 > 0):
            theta1 = 2 * math.pi + theta1
        theta = np.linspace(theta2, theta1, resolution)
    points = np.vstack((radius*np.cos(theta) + center[0],
                        radius*np.sin(theta) + center[1]))
    # build the polygon and add it to the axes
    poly = Polygon(points.T, closed=False, **kwargs)
    return poly


def paint_function(content, ax, fig):
    return lambda idx: paint_panda_row(idx, len(content.index), content.iloc[idx], ax, fig)


def paint_panda_row(idx, len, data, ax, fig):
    print('painting')
    ax.cla()
    ax.autoscale(True)
    the_text = "{} of {}".format(idx, len)
    ax.set_title(the_text)
    segs = data[0]
    plot_frame_content(segs, ax)
    fig.canvas.draw()


def main():
    print("bin content")
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    histogram_data = client.histograms.test

    ## cursor_chandigarh = angle_collection.find({"city": "Chandigarh"})
    cursor_histogram = histogram_data.find()
    entries_histogram = list(cursor_histogram)
    data_in = []
    is_first = True
    nr_of_clusters = 0
    for entry in entries_histogram:
        nr_of_clusters = nr_of_clusters + 1
        segs = []
        cluster_data_frame = pd.DataFrame(entry)
        for trajectory in entry["trajectories"]:
            for vector in trajectory:
                bX = float(vector['bX'])
                bY = float(vector['bY'])
                dX = float(vector['dX'])
                dY = float(vector['dY'])
                if is_first:
                    print("bx: %2.4f, by: %2.4f, dx: %2.4f, dy: %2.4f" % (bX, bY, dX, dY))
                    is_first = False
                segs.append(Arrow(bX, bY, dX, dY, width=1, linestyle="-", alpha=0.5, color='green'))
                # segs.append(Arrow(0.0, 0.0, 1.0, 0.0, width=1, linestyle="-", alpha=1.0, color="green"))
        data_in.append([segs, nr_of_clusters])

    print(data_in)
    all_content = DataFrame(data=data_in, columns=["segs", "idx"])
    figure = pyplot.figure()
    ax = figure.add_subplot(1, 1, 1)
    zoom_pan_animate = ZoomPanAnimate()
    my_painter = paint_function(all_content, ax, figure)

    animate = zoom_pan_animate.animate_factory(ax, nr_of_clusters, my_painter)
    figZoom = zoom_pan_animate.zoom_factory(ax, base_scale=1.1)
    figPan = zoom_pan_animate.pan_factory(ax)
    ax.autoscale(True)
    pyplot.gca().set_aspect("equal", adjustable="datalim")

    pyplot.show()


def plot_frame_content(segs, ax):
    # format for segments: "seg x1 y1 x2 y2"
    # format for arcs: "arc center_x center_y radius start_angle end_angle"
    # for debugging reasons we store arc_points as well
    # (following fields are attached to the end of an arc entry: "x0, y0, xn, yn, xc, yc" with 0 first point, n last point, c center point)
    # format for points: point x y
    print('plotting')
    for cur_seg in segs:
        ax.add_patch(cur_seg)


if __name__ == '__main__':
    main()
