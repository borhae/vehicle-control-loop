# -----------------------------------------------------------------------------
# Copyright Joachim Haensel 2019.
# Glumpy based viewer for trajectories
# -----------------------------------------------------------------------------
import inspect
import numpy as np
from glumpy import app, glm, gl, library, gloo
from glumpy.graphics.text import FontManager
from glumpy.transforms import TrackballPan, LinearScale, Viewport, Position
from glumpy.graphics.collections import *

import pymongo
from pandas import DataFrame


def line_path(point0, point1, n=100):
    path = np.zeros((n, 3))
    x_min, y_min, z_min = point0
    x_max, y_max, z_max = point1
    path[:, 0] = np.linspace(x_min, x_max, n, endpoint=True)
    path[:, 1] = np.linspace(y_min, y_max, n, endpoint=True)
    path[:, 2] = np.linspace(z_min, z_max, n, endpoint=True)
    return path


def read_from_database():
    client = pymongo.MongoClient("mongodb://localhost:27017/")
    histogram_data = client.histograms.test3d

    cursor_histogram = histogram_data.find().limit(30)
    entries_histogram = list(cursor_histogram)
    nr_of_clusters = 0
    clusters = []
    for entry in entries_histogram:
        trajectories = []
        database_trajectories = entry["trajectories"]
        print("processed %s with %s trajetories" % (nr_of_clusters, len(database_trajectories)))
        nr_of_clusters = nr_of_clusters + 1
        idx = 0
        for database_trajectory in database_trajectories:
            trajectory = []
            for trajectory_element in database_trajectory:
                vector = trajectory_element['vector2D']
                bX = float(vector['bX'])
                bY = float(vector['bY'])
                bZ = float(trajectory_element['velocity'])
                dX = float(vector['dX'])
                dY = float(vector['dY'])
                trajectory.append([bX, bY, bZ, dX, dY])
            trajectories.append([trajectory, idx])
            idx = idx + 1
        clusters.append(trajectories)
    return clusters


def add_lines_from_database(clusters, paths, idx):
    trajectories = clusters[idx]
    for trajectory, t_idx in trajectories:
        last_z = trajectory[0][2]
        for vec in trajectory:
            b_x = vec[0]
            b_y = vec[1]
            z = vec[2]
            d_x = vec[3]
            d_y = vec[4]
            p_1 = (b_x, b_y, last_z)
            p_2 = (d_x + b_x, d_y + b_y, z)
            paths.append(line_path(p_1, p_2), n=1, color=(1, 0, 0, .25))
            last_z = z


def construct_axis_ticks(paths, transform, viewport, xmax, xmin, ymax, ymin, zmax, zmin):
    n_major = 10 + 1
    n_minor = 50 + 1
    length_major = 0.02
    length_minor = 0.01
    for x in np.linspace(xmin, xmax, n_major)[0:-1]:
        paths.append(line_path((x, ymin, zmin), (x, ymin + length_major, zmin)),
                     linewidth=2.0, color=(0, 0, 0, 1))
    for x in np.linspace(xmin, xmax, n_minor)[0:-1]:
        paths.append(line_path((x, ymin, zmin), (x, ymin + length_minor, zmin)),
                     linewidth=1.0, color=(0, 0, 0, 1))
    length_major = 0.04
    length_minor = 0.02
    for y in np.linspace(ymin, ymax, n_major)[0:-1]:
        paths.append(line_path((xmax, y, zmin), (xmax - length_major, y, zmin)),
                     linewidth=2.0, color=(0, 0, 0, 1))
    for y in np.linspace(ymin, ymax, n_minor)[0:-1]:
        paths.append(line_path((xmax, y, zmin), (xmax - length_minor, y, zmin)),
                     linewidth=1.0, color=(0, 0, 0, 1))
    for z in np.linspace(zmin, zmax, n_major)[0:-1]:
        paths.append(line_path((xmin, ymin, z), (xmin + length_major, ymin, z)),
                     linewidth=2.0, color=(0, 0, 0, 1))
    for z in np.linspace(zmin, zmax, n_minor)[0:-1]:
        paths.append(line_path((xmin, ymin, z), (xmin + length_minor, ymin, z)),
                     linewidth=1.0, color=(0, 0, 0, 1))


def construct_gridlines(paths, xmax, xmin, ymax, ymin, zmin):
    # Grids
    # -------------------------------------
    n = 100 + 1
    X = np.linspace(xmin, xmax, n)[1:-1]
    for x in X:
        paths.append(line_path((x, ymin, zmin), (x, ymax, zmin)),
                     linewidth=1.0, color=(0, 0, 0, .25))
    Y = np.linspace(ymin, ymax, n)[1:-1]
    for y in Y:
        paths.append(line_path((xmin, y, zmin), (xmax, y, zmin)),
                     linewidth=1.0, color=(0, 0, 0, .25))


def construct_outer_frame(paths, transform, viewport, xmax, xmin, ymax, ymin, zmax, zmin):
    # Outer Frame
    # -------------------------------------
    paths.append(line_path((xmin, ymin, zmin), (xmax, ymin, zmin)), linewidth=2.0)
    paths.append(line_path((xmin, ymax, zmin), (xmax, ymax, zmin)), linewidth=2.0)
    paths.append(line_path((xmax, ymin, zmin), (xmax, ymax, zmin)), linewidth=2.0)
    paths.append(line_path((xmin, ymin, zmin), (xmin, ymax, zmin)), linewidth=2.0)
    mid_x = ((xmax - xmin) / 2) + xmin
    mid_y = ((ymax - ymin) / 2) + ymin
    print("xmin %s xmax %s ymin %s ymax %s" % (xmin, xmax, ymin, ymax))
    paths.append(line_path((mid_x, mid_y, zmin), (mid_x, mid_y, zmax)), linewidth=2.0)


def main():
    clusters = read_from_database()

    window = app.Window(width=1200, height=1000, color=(1, 1, 1, 1))
    buck_idx = 0

    x_scale = LinearScale(".x", name="xscale", domain=[-1000, 1000], range=[-10, 10])
    y_scale = LinearScale(".y", name="yscale", domain=[-1000, 1000], range=[-10, 10])
    z_scale = LinearScale(".z", name="zscale", domain=[0, 2000], range=[0, 20])
    trackball = TrackballPan(name="view_projection", aspect=1)
    transform = trackball(Position(x_scale, y_scale, z_scale))
    viewport = Viewport()

    paths = PathCollection("agg", linewidth="shared", color="shared", transform=transform, viewport=viewport)

    @window.event
    def on_draw(dt):
        nonlocal window
        window.clear()
        nonlocal paths
        paths.draw()

    @window.event
    def on_character(text):
        nonlocal buck_idx
        buck_idx = buck_idx + 1
        nonlocal paths
        p_len = len(paths)
        print(p_len)
        paths = PathCollection("agg", linewidth="shared", color="shared", transform=transform, viewport=viewport)
        construct_outer_frame(paths, transform, viewport, x_max, x_min, y_max, y_min, z_max, z_min)
        construct_gridlines(paths, x_max, x_min, y_max, y_min, z_min)
        add_lines_from_database(clusters, paths, buck_idx)
        p_len = len(paths)
        print(p_len)
        paths.draw()

    x_min, x_max = -100, 100
    y_min, y_max = -100, 100
    z_min, z_max = 0, 200

    construct_outer_frame(paths, transform, viewport, x_max, x_min, y_max, y_min, z_max, z_min)
    construct_gridlines(paths, x_max, x_min, y_max, y_min, z_min)
    construct_axis_ticks(paths, transform, viewport, x_max, x_min, y_max, y_min, z_max, z_min )

    print(len(clusters))
    for idx in range(0, 20):
        print("adding %s" % idx)
        add_lines_from_database(clusters, paths, idx)

    trackball["phi"] = 45
    trackball["zoom"] = 25
    trackball["theta"] = 65
    window.attach(paths["transform"])
    window.attach(paths["viewport"])
    app.run()


if __name__ == '__main__':
    main()
