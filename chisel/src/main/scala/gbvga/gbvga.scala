package gbvga

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

class GbVga extends Module with GbConst {
  val io = IO(new Bundle {
    /* Game boy input signals */
    val gb = Input(new Gb())
    /* VGA output signals */
    val vga_hsync = Output(Bool())
    val vga_vsync = Output(Bool())
    val vga_color = Output(new VgaColors())
  })

  /* GameBoy write */
  val gbwrite = Module(new GbWrite(2, debug_simu=false, aformal=false))
  gbwrite.io.gb := io.gb

  /* Mem Vga */
//  val memvga = Module(new MemVga())
  val memvga = Module(new MemVgaZoom())
  io.vga_hsync := memvga.io.vga_hsync
  io.vga_vsync := memvga.io.vga_vsync
  io.vga_color <> memvga.io.vga_color

  /* dual port ram connection */
  val mem = Mem(GBWIDTH*GBHEIGHT, UInt(2.W))
  when(gbwrite.io.Mwrite) {
    mem(gbwrite.io.Maddr) := gbwrite.io.Mdata
  }
  val last_read_value = RegInit(0.U(2.W))
  when(memvga.io.mem_read) {
    memvga.io.mem_data := RegNext(mem(memvga.io.mem_addr))
    last_read_value := memvga.io.mem_data
  }.otherwise {
    memvga.io.mem_data := last_read_value
  }

}

object GbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new GbVga())))
}
