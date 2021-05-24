import sys
import csv
import getopt
import serial
import logging
from cocotb.triggers import Timer
from cocotb.triggers import FallingEdge
from cocotb.triggers import RisingEdge

from PIL import Image, ImageDraw

class VGA(object):
    H_DISPLAY = 640  # horizontal display width
    H_FRONT = 8      # front porch
    H_SYNC = 96      # sync width
    H_BACK = 40      # back porch
    V_SYNC = 4       # sync width
    V_BACK = 25      # back porch
    V_TOP = 4        # top border
    V_DISPLAY = 480  # vertical display width
    V_BOTTOM = 14    # bottom border
    H_SYNC_START = H_DISPLAY + H_FRONT
    H_SYNC_END = H_DISPLAY + H_FRONT + H_SYNC - 1
    H_MAX = H_DISPLAY + H_BACK + H_FRONT + H_SYNC - 1
    V_SYNC_START = V_DISPLAY + V_BOTTOM
    V_SYNC_END = V_DISPLAY + V_BOTTOM + V_SYNC - 1
    V_MAX = V_DISPLAY + V_TOP + V_BOTTOM + V_SYNC - 1


class GbScreenView(object):
    # Color from wikipedia  Game_Boy page
    # Green
    COLOR = ["#0F380F", "#8BAC0F", "#306230", "#9BBC0F"]
    #       width, height
    GBSIZE = (160, 144)
    # Black
    #COLOR = ["#000000", "#555555", "#AAAAAA", "#FFFFFF"]

    def __init__(self, log=None):
        self.image = []
        self.vga_image = None
        if log is None:
            self._log = logging
        else:
            self._log = log

    def _wait_vsync(self, freader):
        # Find rising edge of vsync
        for curtime, hsync, d1, clk, d0, vsync in freader:
            if int(vsync) == 1:
                return

    def _wait_hsync(self, freader):
        # Find rising edge of hsync
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
            next(freader)
            vsync_old = 0
            hsync_old = 0
            self._wait_vsync(freader)
            for j in range(self.GBSIZE[1]):
                self._wait_hsync(freader)
                line = []
                for i in range(self.GBSIZE[0]):
                    line.append(self._wait_clk_fall(freader))
                self.image.append(line)

    def read_signal_integer(self, signal):
        try:
            return signal.value.integer
        except ValueError:
            return 0

    async def vga_2_image(self, clk25, hsync, vsync, red, green, blue, reset):
        """ Thread that read image from vga signals """
        colcount = 0
        linecount = 0
        await FallingEdge(reset)
        display = False
        self.vga_image = [[]]
        old_hsync = 0
        while True:
            await RisingEdge(clk25)
            display = ((colcount >= VGA.H_BACK) and
                            (colcount <= (VGA.H_BACK + VGA.H_DISPLAY)))
            if display:
                if len(self.vga_image[linecount]) <= colcount: 
                    self.vga_image[linecount].append((self.read_signal_integer(red),
                                                      self.read_signal_integer(green),
                                                      self.read_signal_integer(blue)))
                else:
                    self.vga_image[linecount][colcount] = (self.read_signal_integer(red),
                                                           self.read_signal_integer(green),
                                                           self.read_signal_integer(blue))

            if hsync.value.integer == 0:
                colcount = 0
            else:
                colcount += 1
            if old_hsync == 0 and (hsync.value.integer == 1):
                self._log.info("line {}".format(linecount))
                self.vga_image.append([])
                linecount += 1

            if vsync.value.integer == 0:
                linecount = 0

            old_hsync = hsync.value.integer

    def mem_2_image(self, mem):
        for j in range(self.GBSIZE[1]):
            line = []
            for i in range(self.GBSIZE[0]):
                pixnum = j*self.GBSIZE[0] + i
                try:
                    bytevalue = mem[pixnum]
                except KeyError as err:
                    bytevalue = 0
                line.append(bin(bytevalue).split('b')[-1])
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
        im.save("test.png", "PNG")

    def vga_show(self):
        if self.vga_image is None:
            raise Exception("No vga image to display")
        vgawidth = max(set([len(line) for line in self.vga_image]))
        vgaheight = len(self.vga_image)
        im = Image.new("RGB", (vgawidth, vgaheight), "#000000")
        d = ImageDraw.Draw(im)
        for i, line in enumerate(self.vga_image):
            for j, pix in enumerate(line):
                color = "#{:02X}{:02X}{:02X}".format(pix[0] << 2,
                                                     pix[1] << 2,
                                                     pix[2] << 2)
                d.rectangle([(j, i), (j, i)], fill=color)
        im.show()
        im.save("test.png", "PNG")


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
                waitime = newtime - oldtime
                await Timer(waitime, units="ns")
                shsync <= int(hsync)
                svsync <= int(vsync)
                sdata <= int(d1)*2 + int(d0)
                sclk <= int(clk)
                oldtime = int(float(curtime)*1.e9)

