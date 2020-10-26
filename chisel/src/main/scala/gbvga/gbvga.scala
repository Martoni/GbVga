package gbvga

import chisel3._
import chisel3.util._ 
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}


class GbVga extends Module {
  val io = IO(new Bundle {
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

  io.vga_hsync := DontCare
  io.vga_vsync := DontCare
  io.vga_red   := DontCare
  io.vga_green := DontCare
  io.vga_blue  := DontCare

}

object GbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new GbVga())))
}
