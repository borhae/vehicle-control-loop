import glob

import pandas as pd
import matplotlib.pyplot as pyplot
import matplotlib
import argparse
import csv
import math
import re
import numpy as np

from matplotlib.patches import Circle
from matplotlib.patches import Arrow
from matplotlib.patches import Polygon

from pathlib import PurePath

from os import walk

from pandas import DataFrame



def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("path", nargs="*")
    args = parser.parse_args()

    print("histogram")
    reg_paths = []
    for cur_reg in args.path:
        paths = glob.glob(cur_reg)
        reg_paths.append(paths)
    path_pairs = DataFrame(data=reg_paths).transpose()
    matplotlib.rc('xtick', labelsize=6)
    matplotlib.rc('ytick', labelsize=6)
    for idx, path_pair in path_pairs.iterrows():
        for path in path_pair:
            if (path):
                data = pd.read_csv(path)
                print(data)
                print(data.describe())
                fig, ax = pyplot.subplots()
                fig = data.hist(ax=ax, bins=100)
                ax.set_yscale("log")
                [x.title.set_size(9) for x in fig.ravel()]
    pyplot.show()


if __name__ == '__main__':
    main()
