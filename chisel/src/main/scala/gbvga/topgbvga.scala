package gbvga

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}


class TopGbVga extends RawModule {

  /************/
  /** outputs */
  /* Clock and reset */
  val clock = IO(Input(Clock()))
  val resetn = IO(Input(Bool()))
  val pll_rstn = IO(Output(Bool()))

  /* game boy signals */
  val gb_hsync = IO(Input(Bool()))
  val gb_vsync = IO(Input(Bool()))
  val gb_clk = IO(Input(Bool()))
  val gb_data = IO(Input(UInt(2.W)))

  /* Vga */
  val hsync = IO(Output(Bool()))
  val vsync = IO(Output(Bool()))
  val red = IO(Output(UInt(6.W)))
  val green = IO(Output(UInt(6.W)))
  val blue = IO(Output(UInt(6.W)))

  withClockAndReset(clock, ~resetn) {
    /* Activate pll at start*/
    pll_rstn := true.B
   
    /* synchronize gameboy input signals with clock */
    val shsync = ShiftRegister(gb_hsync,2)
    val svsync = ShiftRegister(gb_vsync,2)
    val sclk   = ShiftRegister(gb_clk  ,2)
    val sdata  = ShiftRegister(gb_data ,2)

    /* top GbVga module instantiation */
    val gbVga = Module(new GbVga())
    gbVga.io.gb_hsync := shsync
    gbVga.io.gb_vsync := svsync
    gbVga.io.gb_clk   := sclk
    gbVga.io.gb_data  := sdata
    hsync := gbVga.io.vga_hsync
    vsync := gbVga.io.vga_vsync
    red   := gbVga.io.vga_red
    green := gbVga.io.vga_green
    blue  := gbVga.io.vga_blue

  }
}

object TopGbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new TopGbVga())))
}
