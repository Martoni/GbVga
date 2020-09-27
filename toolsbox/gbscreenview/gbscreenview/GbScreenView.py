import sys
import csv
import getopt
import serial
import logging
from PIL import Image, ImageDraw

class GbScreenView(object):
    # Color from wikipedia  Game_Boy page
    # Green
    COLOR = [
            "#0F380F",
            "#8BAC0F",
            "#306230",
            "#9BBC0F"]
    # Black
    #COLOR = ["#000000", "#555555", "#AAAAAA", "#FFFFFF"]

    def __init__(self):
        self.image = []

    def _wait_vsync(self, freader):
        # Find rising edge of vsync
        for curtime, hsync, d1, clk, d0, vsync in freader:
            if int(vsync) == 1:
                return

    def _wait_hsync(self, freader):
        # Find falling edge of hsync
        for curtime, hsync, d1, clk, d0, vsync in freader:
            if int(hsync) == 1:
                return

    def _wait_clk_fall(self, freader):
        old_clk = 0
        for curtime, hsync, d1, clk, d0, vsync in freader:
            if int(clk) == 0 and int(old_clk) == 1:
                strvalue = f"{d1.strip()}{d0.strip()}"
                return strvalue
            old_clk = clk

    def read_csv(self, filename):
        """ Read first image """
        with open(filename) as csvfile:
            freader = csv.reader(csvfile, delimiter=',')
            print("titles")
            print(next(freader))
            vsync_old = 0
            hsync_old = 0
            self._wait_vsync(freader)
            for j in range(140):
                self._wait_hsync(freader)
                line = []
                for i in range(160):
                    line.append(self._wait_clk_fall(freader))
                self.image.append(line)

    def show(self):
        if self.image == []:
            raise Exception("Read data first")
        square = 3 
        im = Image.new("RGB", (160*square, 140*square), "#000000")
        d = ImageDraw.Draw(im)
        color = list(self.COLOR)
        color.reverse()
        for j in range(140):
            for i in range(160):
                d.rectangle([(i*square, j*square),
                             (i*square+(square-1), j*square+(square-1))],
                             fill=color[int(self.image[j][i], 2)])
        im.show()
