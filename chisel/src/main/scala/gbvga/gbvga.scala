package gbvga

import chisel3._
import chisel3.util._


class GbVga extends Module {
  val io = IO({
    /* Game boy input signals */
    val gb_hsync = Input(Bool())
    val gb_vsync = Input(Bool())
    val gb_clk = Input(Bool())
    val gb_data = Input(Bool())
    /* VGA output signals */
    val vga_hsync = Output(Bool())
    val vga_vsync = Output(Bool())
    val vga_red = Output(Bool())
    val vga_green = Output(Bool())
    val vga_blue = Output(Bool())
  })



}

object GbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new GbVga())))
}
