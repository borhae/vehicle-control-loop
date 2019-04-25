import glob

import argparse
import csv
import math
import re
import numpy as np
import json

from mayavi.mlab import *

from pathlib import PurePath

from os import walk

from pandas import DataFrame


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-g", action="store_true")
    parser.add_argument("path", nargs="*")
    args = parser.parse_args()

    print(args.path)

    if args.g:
        print("with histogram")
        plot_trajectory_via_histogram(args.path[0], args.path[1])
    else:
        reg_paths = []
        print("resolving regex")
        for cur_reg in args.path:
            paths = glob.glob(cur_reg)
            reg_paths.append(paths)
        print(reg_paths)
        for paths in reg_paths:
            for path in paths:
                plot_trajectory_json(path)


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


def rotate_origin_only(x, y, radians):
    """Only rotate a point around the origin (0, 0)."""
    xx = x * math.cos(radians) + y * math.sin(radians)
    yy = -x * math.sin(radians) + y * math.cos(radians)
    return xx, yy


def as_int(s):
    number, _ = s
    try:
        return int(number), ''
    except ValueError:
        return sys.maxint, number


def plot_trajectory_via_histogram(path_trajectory, path_histogram):
    with open(path_histogram) as histogram_file:
        histogram = json.load(histogram_file)
        plot_data = []
        with open(path_trajectory) as trajectories_file:
            trajectories = json.load(trajectories_file)
            sorted_histogram = histogram  # sorted(histogram.items(), key=as_int)
            color_list = cm.hsv(np.linspace(0, 1, len(sorted_histogram)))
            colors = iter(color_list)
            print(len(sorted_histogram))
            print(len(color_list))
            for equivalence_class_id in sorted_histogram:
                line_color = next(colors)
                for trajectory_id in histogram[equivalence_class_id]:
                    trajectory = trajectories[str(trajectory_id)]
                    x = []
                    y = []
                    z = []
                    first_trj_elem = next(iter(trajectory), None)
                    xB0 = first_trj_elem['_vector']['_bX']
                    yB0 = first_trj_elem['_vector']['_bY']
                    xD0 = first_trj_elem['_vector']['_dX']
                    yD0 = first_trj_elem['_vector']['_dY']
                    angle = math.atan2(yD0, xD0)
                    for list_item in trajectory:
                        x_i = list_item['_vector']['_bX'] - xB0
                        y_i = list_item['_vector']['_bY'] - yB0
                        x_i, y_i = rotate_origin_only(x_i, y_i, angle)
                        x.append(x_i)
                        y.append(y_i)
                        z.append(list_item['_velocity'])
                    plot_data.append((x, y, z, line_color))

        for cur_series in plot_data:
            x_ser, y_ser, z_ser, series_color = cur_series
            plot3d(x_ser, y_ser, z_ser, tube_radius=0.025, colormap='Spectral')
        pyplot.show()


def plot_trajectory_json(path):
    pure_path = PurePath(path)
    print(pure_path.suffix)
    if pure_path.suffix == '.json':
        print("identified json")
        plot_data = []
        with open(path) as json_file:
            data = json.load(json_file)
            sorted_data = sorted(data.items(), key=as_int)
            for key, value in sorted_data:
                print(key)
                x = []
                y = []
                z = []
                first_trj_elem = next(iter(value), None)
                xB0 = first_trj_elem['_vector']['_bX']
                yB0 = first_trj_elem['_vector']['_bY']
                xD0 = first_trj_elem['_vector']['_dX']
                yD0 = first_trj_elem['_vector']['_dY']
                angle = math.atan2(yD0, xD0)
                for list_item in value:
                    x_i = list_item['_vector']['_bX'] - xB0
                    y_i = list_item['_vector']['_bY'] - yB0
                    x_i, y_i = rotate_origin_only(x_i, y_i, angle)
                    x.append(x_i)
                    y.append(y_i)
                    z.append(list_item['_velocity'])
                plot_data.append((x, y, z))

        mpl.rcParams['legend.fontsize'] = 10

        fig = pyplot.figure()
        ax = fig.gca(projection='3d')
        for cur_series in plot_data:
            X, Y, Z = cur_series
            ax.plot(X, Y, Z, label='trajectory', alpha=0.2)
        pyplot.show()
    else:
        print("this is not a json file!")


if __name__ == '__main__':
    main()
