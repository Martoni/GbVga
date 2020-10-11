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
from cocotb.triggers import RisingEdge
from cocotb.triggers import FallingEdge
from cocotb.triggers import ClockCycles
from gbscreenview import GbScreenView


class TestGbWrite(object):
    #CLK_PER = (40, "ns") #25Mhz
    CLK_PER = (125, "ns") #8Mhz
    CSV_FILENAME = "../../assets/screenshoot/beautyandbeast.csv"

    def __init__(self, dut):
        self._dut = dut
        self.log = dut._log
        self.clk = dut.clock
        self.rst = dut.reset
        self._clock_thread = cocotb.fork(
                Clock(self.clk, *self.CLK_PER).start())
        self._gsv = GbScreenView()

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

    async def reset(self):
        self.rst <= 1
        await Timer(100, units="ns")
        self.rst <= 0
        self._gbsigs = cocotb.fork(self._gsv.gen_waves(
                        self._dut.io_GBHsync,
                        self._dut.io_GBVsync,
                        self._dut.io_GBClk,
                        self._dut.io_GBData,
                        self.log,
                        self.CSV_FILENAME))

        await RisingEdge(self.clk)

@cocotb.test()
async def minimal_clocking(dut):
    fname = "minimal_clocking"
    tgw = TestGbWrite(dut)
    tgw.display_config()
    await Timer(1)
    tgw.log.info("Running test {}".format(fname))
    await tgw.reset()
    await Timer(17, units="ms")
    tgw.log.info("End of {} test".format(fname))
