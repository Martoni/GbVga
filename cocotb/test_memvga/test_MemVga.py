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

class TestMemVga(object):
    CLK_PER = (40, "ns") #25Mhz

    def __init__(self, dut):
        self._dut = dut
        self.log = dut._log
        self.clk = dut.clock
        self.rst = dut.reset
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
        await RisingEdge(self.clk)

@cocotb.test()
async def one_frame(dut):
    fname = "one_frame"
    tmv = TestMemVga(dut)
    tmv.display_config()
    await Timer(1)
    tmv.log.info("Running test {}".format(fname))
    await tmv.reset()
    await FallingEdge(dut.io_vsync)
