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
    val vga_color = Output(new VgaColors())
  })

  val hvsync = Module(new HVSync()) // Synchronize VGA module
  io.vga_hsync := hvsync.io.hsync
  io.vga_vsync := hvsync.io.vsync

  io.vga_color := VGA_BLACK
  when(hvsync.io.display_on){
    io.vga_color   := GbColors(0)
  }

//XXX
io.mem_addr  := DontCare
io.mem_read  := DontCare
//XXX

}

object MemVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new MemVga())))
}
