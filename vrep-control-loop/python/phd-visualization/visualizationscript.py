import glob

import matplotlib.pyplot as pyplot
import argparse
import csv 
import math
import re
import numpy as np 
import json

from matplotlib.patches import Circle
from matplotlib.patches import Arrow
from matplotlib.patches import Polygon

from pathlib import PurePath

from os import walk

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
    return lambda idx : paint_panda_row(idx, len(content.index), content.iloc[idx], ax, fig)


def paint_panda_row(idx, len, data, ax, fig):
        ax.cla()
        ax.autoscale(True)
        the_text = "{} of {}".format(idx, len)
        ax.set_title(the_text)
        segs = data[0]
        arcs = data[1]
        arc_points = data[2]
        points = data[3]
        lines = data[4]
        plotFrameContent(segs, arcs, arc_points, points, lines, ax, idx)
        fig.canvas.draw()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-v", nargs="+", action="append")
    parser.add_argument("-R", nargs="+", action="append")
    parser.add_argument("path", nargs="*")
    args = parser.parse_args()

    print("hallo")
    print(args.path)
    print(args.v)

    if(args.v):
        print("file list for slideshow")
        pathlists = crawlDir(args.v[0])
        path_pairs = DataFrame(data=pathlists).transpose()

        data_in = []
        for idx, path_pair in path_pairs.iterrows():
            segs = []
            arcs = []
            arc_points = []
            points = []
            lines = []
            for path in path_pair:
                if(path):
                    readFrameContent(path, segs, arcs, arc_points, points, lines, idx)
            data_in.append([segs, arcs, arc_points, points, lines, idx])
        all_content = DataFrame(data=data_in, columns=["segs", "arcs", "arc_points", "points", "lines", "idx"])
        figure = pyplot.figure()
        ax = figure.add_subplot(1, 1, 1)
        zoom_pan_animate = ZoomPanAnimate()
        my_painter = paint_function(all_content, ax, figure)

        animate = zoom_pan_animate.animate_factory(ax, len(path_pairs), my_painter)
        figZoom = zoom_pan_animate.zoom_factory(ax, base_scale=1.1)
        figPan = zoom_pan_animate.pan_factory(ax)
        ax.autoscale(True)
        pyplot.gca().set_aspect("equal", adjustable="datalim")

        pyplot.show()
    elif args.R:
        print("fileList from regex")
        reg_paths = []
        for cur_reg in args.R[0]:
            paths = glob.glob(cur_reg)
            reg_paths.append(paths)
        path_pairs = DataFrame(data=reg_paths).transpose()

        data_in = []
        for idx, path_pair in path_pairs.iterrows():
            segs = []
            arcs = []
            arc_points = []
            points = []
            lines = []
            for path in path_pair:
                if (path):
                    readFrameContent(path, segs, arcs, arc_points, points, lines, idx)
            data_in.append([segs, arcs, arc_points, points, lines, idx])
        all_content = DataFrame(data=data_in, columns=["segs", "arcs", "arc_points", "points", "lines", "idx"])
        figure = pyplot.figure()
        ax = figure.add_subplot(1, 1, 1)
        zoom_pan_animate = ZoomPanAnimate()
        my_painter = paint_function(all_content, ax, figure)

        animate = zoom_pan_animate.animate_factory(ax, len(path_pairs), my_painter)
        figZoom = zoom_pan_animate.zoom_factory(ax, base_scale=1.1)
        figPan = zoom_pan_animate.pan_factory(ax)
        ax.autoscale(True)
        pyplot.gca().set_aspect("equal", adjustable="datalim")

        pyplot.show()
    else:
        segs = []
        arcs = []
        arc_points = []
        points = []
        lines = []

        figure = pyplot.figure()
        ax = figure.add_subplot(1, 1, 1)

        reg_paths = []
        print("resolving regex")
        for cur_reg in args.path:
            paths = glob.glob(cur_reg)
            reg_paths.append(paths)
        print(reg_paths)
        for paths in reg_paths:
            for path in paths:
                readFrameContent(path, segs, arcs, arc_points, points, lines, 0)

        plotFrameContent(segs, arcs, arc_points, points, lines, ax, 0)
        zoom_pan = ZoomPanAnimate()
        figZoom = zoom_pan.zoom_factory(ax, base_scale=1.1)
        figPan = zoom_pan.pan_factory(ax)
        ax.autoscale(True)
        pyplot.gca().set_aspect("equal", adjustable="datalim")
        pyplot.show()


def crawlDir(paths):
    print("crawling")
    print(paths)
    pathlists = []
    for path_name in paths:
        cur_series_list = []
        path = PurePath(path_name)
        path_stem = path.stem
        print("stem")
        print(path_stem)
        for (_, _, filenammes) in walk(path.parent):
            for cur_fname in filenammes:
                if(re.match(r"^" + re.escape(path_stem) + r"\d\d\d\d\d\d", cur_fname)):
                    path_to_add = path.parent / cur_fname
                    cur_series_list.append(path_to_add)
        pathlists.append(cur_series_list)
    return pathlists


def plotFrameContent(segs, arcs, arc_points, points, lines, ax, frame_idx):
    # format for segments: "seg x1 y1 x2 y2"
    # format for arcs: "arc center_x center_y radius start_angle end_angle"
    # for debugging reasons we store arc_points as well
    # (following fields are attached to the end of an arc entry: "x0, y0, xn, yn, xc, yc" with 0 first point, n last point, c center point)
    # format for points: point x y
    for cur_seg in segs:
        ax.add_patch(cur_seg)
    for cur_arc in arcs:
        ax.add_patch(cur_arc)
    for cur_arc_point in arc_points:
        ax.add_patch(cur_arc_point)
    for cur_point in points:
        ax.add_patch(cur_point)
    for cur_line in lines:
        ax.add_patch(cur_line)


def readFrameContent(path, segs, arcs, arc_points, points, lines, idx):
    with open(path) as csv_file:
        data = csv.reader(csv_file, delimiter=" ")
        last_tip_x = 0
        last_tip_y = 0
        last_line_tip_x = 0
        last_line_tip_y = 0
        first_point_pass = True
        first_line_point_pass = True
        for row in data:
            if(row[0] == "seg"):
                if (len(row) == 6):
                    arrow_color = row[5]
                else:
                    arrow_color = "green"
                segs.append(Arrow(float(row[1]), float(row[2]), float(row[3]) - float(row[1]), float(row[4]) - float(row[2]), width=1, linestyle="-", alpha=0.5, color=arrow_color))
            elif (row[0] == "arc"):
                radius = float(row[3])
                st_an = float(row[4])
                en_an = float(row[5])
                arcs.append(arc_patch([float(row[1]), float(row[2])], radius, st_an, en_an, dir=row[12], idx=idx, alpha=0.5, linewidth=5.0, fill=False, color="red"))
                arc_points.append(Circle([float(row[6]), float(row[7])], radius=0.5, color="magenta", alpha=0.3))
                arc_points.append(Circle([float(row[8]), float(row[9])], radius=0.5, color="yellow", alpha=0.3))
            elif (row[0] == "point"):
                if(len(row) == 4):
                    arrow_color = row[3]
                else:
                    arrow_color = "blue"
                if(not first_point_pass):
                    new_tip_x = float(row[1])
                    new_tip_y = float(row[2])
                    points.append(Arrow(last_tip_x, last_tip_y, new_tip_x - last_tip_x, new_tip_y - last_tip_y, width=100, fill=True, linestyle="-", alpha=0.5, color=arrow_color))
                    last_tip_x = new_tip_x
                    last_tip_y = new_tip_y
                else:
                    points.append(Arrow(float(row[1]), float(row[2]), 0.0, 0.0, width=1, linestyle="-", alpha=0.5, color=arrow_color))
                    last_tip_x = float(row[1])
                    last_tip_y = float(row[2])
                    first_point_pass = False
            elif (row[0] == "line"):
                if(len(row) == 4):
                    arrow_color = row[3]
                else:
                    arrow_color = "blue"
                if(not first_line_point_pass):
                    new_line_tip_x = float(row[1])
                    new_line_tip_y = float(row[2])
                    xy = np.array([[last_line_tip_x, last_line_tip_y], [new_line_tip_x, new_line_tip_y]])
                    lines.append(Polygon(xy, closed=False, fill=False, linestyle="-", alpha=0.5, color=arrow_color))
                    last_line_tip_x = new_line_tip_x
                    last_line_tip_y = new_line_tip_y
                else:
                    xy = np.array([[float(row[1]), float(row[2])], [float(row[1]), float(row[2])]])
                    lines.append(Polygon(xy, closed=False, fill=False, linestyle="-", alpha=0.5, color=arrow_color))
                    last_line_tip_x = float(row[1])
                    last_line_tip_y = float(row[2])
                    first_line_point_pass = False
            elif row[0] == "polyline":
                poly_color = row[1]
                should_close = row[2]
                flat_xy = np.array(row[2:len(row) - 1])
                xy = np.reshape(flat_xy, (-1, 2))
                print(xy)
                print(path)
                lines.append(Polygon(xy, closed=should_close, fill=False, linestyle="-", alpha=0.5, color=poly_color))
            else:
                print("what? unknown found")
                print(row)


if __name__ == '__main__':
    main()
