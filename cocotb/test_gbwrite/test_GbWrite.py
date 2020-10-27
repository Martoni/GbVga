#! /usr/bin/python3
# -*- coding: utf-8 -*-
#-----------------------------------------------------------------------------
# Author:   Fabien Marteau <mail@fabienm.eu>
# Created:  09/10/2020
#-----------------------------------------------------------------------------
""" test_debounce
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
from image_test import mem_image

class TestGbWrite(object):
    CLK_PER = (40, "ns") #25Mhz
    #CLK_PER = (80, "ns") #12.5Mhz
    #CLK_PER = (100, "ns") #10Mhz
    CSV_FILENAME = "../../assets/screenshoot/beautyandbeast.csv"

    def __init__(self, dut):
        self._dut = dut
        self.log = dut._log
        self.clk = dut.clock
        self.rst = dut.reset
        self._clock_thread = cocotb.fork(
                Clock(self.clk, *self.CLK_PER).start())
        self._gsv = GbScreenView()
        self._td = cocotb.fork(
                self.time_display(step=(1, "ms")))
        self._mem = {}

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

    async def memory_writer(self):
        while True:
            await RisingEdge(self._dut.clock)
            if self._dut.io_Mwrite.value.integer > 0:
                addr  = self._dut.io_Maddr.value.integer
                data = self._dut.io_Mdata.value.integer
                self._mem[addr] = data

    async def gb_mw_monitor(self):
        while True:
            await FallingEdge(self._dut.io_GBClk)
            await Timer(1)
            if self._dut.io_Mwrite.value.integer == 0:
                msg = "No mwrite at GBClk falling edge"
                self.log.error(msg)
                raise TestFailure(msg)

    def display_memory(self):
        self._gsv.mem_2_image(self._mem)
        self._gsv.show()

    async def reset(self):
        self.rst <= 1
        await Timer(100, units="ns")
        self.rst <= 0
        self._mem_writer = cocotb.fork(self.memory_writer())
        self._gbsigs = cocotb.fork(self._gsv.gen_waves(
                        self._dut.io_GBHsync,
                        self._dut.io_GBVsync,
                        self._dut.io_GBClk,
                        self._dut.io_GBData,
                        self.log,
                        self.CSV_FILENAME))

        await RisingEdge(self.clk)

@cocotb.test()
async def one_frame(dut):
    fname = "one_frame"
    tgw = TestGbWrite(dut)
    tgw.display_config()
    await Timer(1)
    tgw.log.info("Running test {}".format(fname))
    await tgw.reset()

    # Beginning of first full frame
    await RisingEdge(dut.io_GBVsync)
    tgw.log.info("GBVsync rise")
    await FallingEdge(dut.io_GBVsync)
    tgw.log.info("GBVsync fall")

    # Beginning of second frame
    await RisingEdge(dut.io_GBVsync)
    tgw.display_memory()

    pixelcount = dut.io_countcol.value.integer
    rightpixelcount = 0x5A00
    if pixelcount != rightpixelcount:
        msg = ("Wrong value of pixel count {}, should be {}"
                .format(pixelcount, rightpixelcount))
        tgw.log.error(msg)
        raise TestFailure(msg)
    tgw.log.info("GBVsync rise")
    await FallingEdge(dut.io_GBVsync)
    tgw.log.info("GBVsync fall")
    tgw.log.info("End of {} test".format(fname))
