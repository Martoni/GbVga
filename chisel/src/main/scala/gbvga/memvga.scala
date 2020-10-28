package gbvga

import chisel3._
import chisel3.util._ 
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import GbConst._

class MemVga extends Module {
  val io = IO(new Bundle {
    /* memory read interface */
    val mem_addr  = Output(UInt((log2Ceil(GBWITH*GBHEIGHT)).W))
    val mem_data  = Input(UInt(2.W))
    val mem_read = Output(Bool())

    /* VGA output signals */
    val vga_hsync = Output(Bool())
    val vga_vsync = Output(Bool())
    val vga_red = Output(Bool())
    val vga_green = Output(Bool())
    val vga_blue = Output(Bool())
  })

//XXX
io.mem_addr  := DontCare
io.mem_read  := DontCare
io.vga_hsync := DontCare
io.vga_vsync := DontCare
io.vga_red   := DontCare
io.vga_green := DontCare
io.vga_blue  := DontCare
//XXX
}


object MemVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new MemVga())))
}
