package gbvga

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}


class MemVga extends Module with GbConst {
  val io = IO(new Bundle {
    /* memory read interface */
    val mem_addr  = Output(UInt((log2Ceil(GBWIDTH*GBHEIGHT)).W))
    val mem_data  = Input(UInt(2.W))
    val mem_read = Output(Bool())

    /* VGA output signals */
    val vga_hsync = Output(Bool())
    val vga_vsync = Output(Bool())
    val vga_color = Output(new VgaColors())
  })

  val vga_height = GBHEIGHT*2
  val vga_width = GBWIDTH*2

  val hvsync = Module(new HVSync()) // Synchronize VGA module
  io.vga_hsync := hvsync.io.hsync
  io.vga_vsync := hvsync.io.vsync

  val gblines = RegInit(0.U((log2Ceil(GBWIDTH)).W))
  val gbcols = RegInit(0.U((log2Ceil(GBHEIGHT)).W))
  val gbpix = RegInit(0.U((log2Ceil(GBWIDTH*GBHEIGHT)).W))

  /* state machine */
  val sInit :: sPixInc :: sLineInc :: sWait :: Nil = Enum(4)
  val state = RegInit(sInit)

  switch(state) {
    is(sInit) {
      when(hvsync.io.display_on){
        state := sPixInc
      }
    }
    is(sPixInc) {
      when(gbcols >= (GBWIDTH - 1).U &&
           gblines <= (GBHEIGHT -1).U){
        state := sLineInc
      }
      when(gblines > (GBHEIGHT - 1).U){
        state := sWait
      }
    }
    is(sLineInc) {
          state := sWait
    }
    is(sWait) {
      when(!hvsync.io.hsync){
        when(gblines < GBHEIGHT.U) {
          state := sPixInc
        }
      }
      when(!hvsync.io.vsync){
        state := sInit
      }
    }
  }

  /* pixel count */
  when(hvsync.io.display_on){
    when(state===sPixInc) {
      gbpix := gbpix + 1.U
      gbcols := gbcols + 1.U
    }
    when(state===sLineInc) {
      gblines := gblines + 1.U
      gbcols := 0.U
    }
  }
  when(state===sInit) {
    gbpix := 0.U
    gbcols := 0.U
    gblines := 0.U
  }

  /* Vga colors */
  io.vga_color := VGA_BLACK
  when(hvsync.io.display_on && (state===sPixInc)){
    io.vga_color := GbColors(io.mem_data)
//    switch(io.mem_data) {
//      is("b00".U) {
//        io.vga_color := GB_GREEN0
//      }
//      is("b01".U) {
//        io.vga_color := GB_GREEN1
//      }
//      is("b10".U) {
//        io.vga_color := GB_GREEN2
//      }
//      is("b11".U) {
//        io.vga_color := GB_GREEN3
//      }
//    }
  }

  /* Memory interface */
  io.mem_addr  := gbpix
  io.mem_read  := true.B

}

object MemVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new MemVga())))
}
