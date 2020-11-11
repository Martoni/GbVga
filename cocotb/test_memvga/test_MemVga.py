#! /usr/bin/python3
# -*- coding: utf-8 -*-
#-----------------------------------------------------------------------------
# Author:   Fabien Marteau <mail@fabienm.eu>
# Created: 28/10/2020
#-----------------------------------------------------------------------------
""" test memvga
"""

import sys
import cocotb
import logging
from cocotb.clock import Clock
from cocotb.triggers import Timer
from cocotb.result import raise_error
from cocotb.result import TestFailure
from cocotb.triggers import RisingEdge
from cocotb.triggers import FallingEdge
from cocotb.triggers import ClockCycles
from gbscreenview import GbScreenView
sys.path.append("../test_gbwrite/")
from image_test import mem_image

class TestMemVga(object):
    CLK_PER = (40, "ns") #25Mhz

    def __init__(self, dut):
        self._dut = dut
        self.log = dut._log
        self.clk = dut.clock
        self.rst = dut.reset
        self._gsv = GbScreenView()
        self.mem_addr = dut.io_mem_addr
        self.mem_data = dut.io_mem_data
        self.mem_read = dut.io_mem_read
        self._clock_thread = cocotb.fork(
                Clock(self.clk, *self.CLK_PER).start())
        self._td = cocotb.fork(
                self.time_display(step=(1, "ms")))

    @classmethod
    def freq(cls, clkper):
        units = {"ps": "GHz",
                 "ns": "MHz",
                 "us": "KHz",
                 "ms": " Hz",
                 "s" : "mHz"}
        return "{} {}".format(1000/float(clkper[0]), units[clkper[1]])

    def display_config(self):
        self.log.info("Freq : {}".format(self.freq(self.CLK_PER)))

    async def time_display(self, step=(1, "us")):
        dtime = 0
        while True:
            self.log.info("t {} {}".format(dtime*step[0], step[1]))
            await Timer(*step)
            dtime += 1

    async def memory_reader(self, mem_image):
        while True:
            await RisingEdge(self.clk)
            if self.mem_read.value.integer > 0:
                try:
                    self.mem_data <= mem_image[self.mem_addr.value.integer]
                except KeyError as err:
                    self.mem_data <= 0
            else:
                self.mem_data <= 0

    async def reset(self):
        self._mem_reader = cocotb.fork(self.memory_reader(mem_image))
        self.rst <= 1
        await Timer(100, units="ns")
        self.rst <= 0
        self._vga_render = cocotb.fork(self._gsv.vga_2_image(
            self.clk, self._dut.io_vga_hsync,
                      self._dut.io_vga_vsync,
                      self._dut.io_vga_color_red,
                      self._dut.io_vga_color_green,
                      self._dut.io_vga_color_blue,
                      self.rst))
        await RisingEdge(self.clk)

@cocotb.test()
async def one_frame(dut):
    fname = "one_frame"
    tmv = TestMemVga(dut)
    tmv.display_config()
    await Timer(1)
    tmv.log.info("Running test {}".format(fname))
    await tmv.reset()
    await FallingEdge(dut.io_vga_vsync)
    with open("vga_image.py", "w") as vi:
        vi.write("vga_image = ")
        vi.write("{}".format(tmv._gsv.vga_image))
    tmv._gsv.vga_show()
