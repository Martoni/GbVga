
SIM=verilator
#SIM=icarus

# Added in head `timescale ...
VL_TIME_UNIT="1ns"
VL_TIME_PRECISION="1ns"

ifeq ($(SIM),verilator)
COMPILE_ARGS=-DCOCOTB_VERILATOR
EXTRA_ARGS += --trace --trace-structs
endif

ifeq ($(SIM),icarus)
COMPILE_ARGS += -DCOCOTB_ICARUS
endif

export COCOTB_REDUCED_LOG_FMT=1

TOPLEVEL=GbWrite
VERILOG_SOURCES = $(PWD)/$(TOPLEVEL)_withtp.v
MODULE=test_$(TOPLEVEL)
include $(shell cocotb-config --makefile)/Makefile.sim

$(PWD)/$(TOPLEVEL)_withtp.v: $(PWD)/../../chisel/$(TOPLEVEL).v
	echo "\`timescale $(VL_TIME_UNIT)/$(VL_TIME_PRECISION)" > $@
	echo "" >> $@
	cat $< >> $@

$(PWD)/../../chisel/$(TOPLEVEL).v:  $(PWD)/../../chisel/src/main/scala/gbvga/gbwrite.scala
	cd $(PWD)/../../chisel/; sbt "runMain gbvga.$(TOPLEVEL)Driver"

mrproper:
	rm -f GbWrite_withtp.v
	rm -rf __pycache__
	rm -f $(PWD)/../../chisel/GbWrite.v
