package gbvga

import chisel3._
import chisel3.util._ 
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}


class GbVga extends Module {
  val io = IO(new Bundle {
    /* Game boy input signals */
    val gb = Input(new Gb())
    /* VGA output signals */
    val vga_hsync = Output(Bool())
    val vga_vsync = Output(Bool())
    val vga_color = Output(new VgaColors())
  })

  io.vga_hsync := DontCare
  io.vga_vsync := DontCare
  io.vga_color.red   := DontCare
  io.vga_color.green := DontCare
  io.vga_color.blue  := DontCare

}

object GbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new GbVga())))
}
