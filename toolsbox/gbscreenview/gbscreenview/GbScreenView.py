import sys
import csv
import getopt
import serial
import logging
from cocotb.triggers import Timer

from PIL import Image, ImageDraw

class GbScreenView(object):
    # Color from wikipedia  Game_Boy page
    # Green
    COLOR = ["#0F380F", "#8BAC0F", "#306230", "#9BBC0F"]
    GBSIZE = (160, 144)
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

    def csv_2_image(self, filename):
        """ Read first image """
        with open(filename) as csvfile:
            freader = csv.reader(csvfile, delimiter=',')
            print("titles")
            print(next(freader))
            vsync_old = 0
            hsync_old = 0
            self._wait_vsync(freader)
            for j in range(self.GBSIZE[1]):
                self._wait_hsync(freader)
                line = []
                for i in range(self.GBSIZE[0]):
                    line.append(self._wait_clk_fall(freader))
                self.image.append(line)


    def read_csv(self, filename):
        """ Read first image """
        print("Warning: read_csv() is deprecated, use csv_2_image() instead")
        self.csv_2_image(filename)


    def show(self):
        if self.image == []:
            raise Exception("Read data first")
        square = 3 
        im = Image.new("RGB",
                       (self.GBSIZE[0]*square, self.GBSIZE[1]*square),
                       "#000000")
        d = ImageDraw.Draw(im)
        color = list(self.COLOR)
        color.reverse()
        for j in range(self.GBSIZE[1]):
            for i in range(self.GBSIZE[0]):
                d.rectangle([(i*square, j*square),
                             (i*square+(square-1), j*square+(square-1))],
                             fill=color[int(self.image[j][i], 2)])
        im.show()

    async def gen_waves(self, shsync, svsync, sclk, sdata, log, filename):
        with open(filename) as csvfile:
            freader = csv.reader(csvfile, delimiter=',')
            print("titles")
            print(next(freader))
            curtime, hsync, d1, clk, d0, vsync =  next(freader)
            shsync <= int(hsync)
            svsync <= int(vsync)
            sdata <= int(d1)*2 + int(d0)
            sclk <= int(clk)
            oldtime = int(float(curtime)*1.e9)
            for curtime, hsync, d1, clk, d0, vsync in freader:
                newtime = int(float(curtime)*1.e9)
                await Timer(newtime - oldtime, units="ns")
                shsync <= int(hsync)
                svsync <= int(vsync)
                sdata <= int(d1)*2 + int(d0)
                sclk <= int(clk)
                oldtime = int(float(curtime)*1.e9)
