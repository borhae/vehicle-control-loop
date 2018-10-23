import matplotlib.pyplot as pyplot
import numpy as np
import math 

from matplotlib.patches import Polygon

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
        theta = np.linspace(theta2, theta1, resolution)
    print(theta)
    points = np.vstack((radius*np.cos(theta) + center[0], 
                        radius*np.sin(theta) + center[1]))
    # build the polygon and add it to the axes
    poly = Polygon(points.T, closed=False, **kwargs)
    return poly

figure = pyplot.figure()
ax = figure.add_subplot(1, 1, 1)
zoom_pan = ZoomPanAnimate()
figZoom = zoom_pan.zoom_factory(ax, base_scale=1.1)
figPan = zoom_pan.pan_factory(ax)
# theta2 = 2 * math.pi - 2.865947
theta2 = - 2.865947
ax.add_patch(arc_patch([0.0, 0.0], 50.0, 2.962577, theta2, "left", 0, resolution=10, fill=False))

ax.autoscale(True)
pyplot.gca().set_aspect("equal", adjustable="datalim")
pyplot.show()