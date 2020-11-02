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

class TestGbVga(object):
    CLK_PER = (40, "ns") #25Mhz
    CSV_FILENAME = "../../assets/screenshoot/beautyandbeast.csv"

    def __init__(self, dut):
        self._dut = dut
        self.log = dut._log
        self.clk = dut.clock
        self.rst = dut.reset
        self._gsv = GbScreenView()
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

    async def reset(self):
        self.rst <= 1
        await Timer(100, units="ns")
        self.rst <= 0
        # gb input
        self._gbsigs = cocotb.fork(self._gsv.gen_waves(
                        self._dut.io_gb_hsync,
                        self._dut.io_gb_vsync,
                        self._dut.io_gb_clk,
                        self._dut.io_gb_data,
                        self.log,
                        self.CSV_FILENAME))

        # vga output
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
    tgv = TestGbVga(dut)
    tgv.display_config()
    await Timer(1)
    tgv.log.info("Running test {}".format(fname))
    await tgv.reset()
    await FallingEdge(dut.io_vga_vsync)
    with open("vga_image.py", "w") as vi:
        vi.write("vga_image = ")
        vi.write("{}".format(tgv._gsv.vga_image))
    tgv._gsv.vga_show()
