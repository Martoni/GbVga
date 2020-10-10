
SIM=verilator
#SIM=icarus

ifeq ($(SIM),verilator)
COMPILE_ARGS=-DCOCOTB_VERILATOR
export COCOTB_HDL_TIMEPRECISION=1ns
EXTRA_ARGS += --trace
endif

ifeq ($(SIM),icarus)
COMPILE_ARGS += -DCOCOTB_ICARUS
endif

export COCOTB_REDUCED_LOG_FMT=1
VERILOG_SOURCES = $(PWD)/../../chisel/GbWrite.v
TOPLEVEL=GbWrite
MODULE=test_$(TOPLEVEL)
include $(shell cocotb-config --makefile)/Makefile.sim


$(PWD)/../../chisel/GbWrite.v:  $(PWD)/../../chisel/src/main/scala/gbvga/gbwrite.scala
	cd $(PWD)/../../chisel/; sbt "runMain gbvga.GbWriteDriver"
