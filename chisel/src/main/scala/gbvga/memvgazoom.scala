/*-----------------------------------------------------------------------------
 Author:   Fabien Marteau <mail@fabienm.eu>
 Created: 11/11/2020
-------------------------------------------------------------------------------
 Zoom Gb Image 
*/

package gbvga

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}


class MemVgaZoom (val stripped: Boolean = false) extends Module with GbConst {
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

  val hvsync = Module(new HVSync()) // Synchronize VGA module
  io.vga_hsync := hvsync.io.hsync
  io.vga_vsync := hvsync.io.vsync

  val wpix = 3
  val hpix = 3

  val xpos = (hvsync.H_DISPLAY - (wpix*GBWIDTH).U)/2.U
  val ypos = (hvsync.V_DISPLAY - (hpix*GBHEIGHT).U)/2.U
  val gb_display = hvsync.io.display_on & (hvsync.io.vpos > ypos) & (hvsync.io.hpos > xpos)

  val gblines = RegInit(0.U((log2Ceil(GBWIDTH)).W))
  val gbcols = RegInit(0.U((log2Ceil(GBHEIGHT)).W))
  val gbpix = RegInit(0.U((log2Ceil(GBWIDTH*GBHEIGHT)).W))

  /* state machine */
  val sInit :: sPixInc :: sLineInc :: sWait :: Nil = Enum(4)
  val state = RegInit(sInit)

  val newgbline = ((hvsync.io.hpos % hpix.U) === (hpix-1).U)
  val newgbcol = ((hvsync.io.vpos % wpix.U) === (wpix-1).U)

  switch(state) {
    is(sInit) {
      when(gb_display){
        state := sPixInc
      }
    }
    is(sPixInc) {
      when((gbcols >= (GBWIDTH - 1).U &&
            gblines <= (GBHEIGHT -1).U) && newgbline){
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
  when(gb_display){
    when((state===sPixInc) && newgbline){  // only incremented on even pos
        gbpix := gbpix + 1.U
        gbcols := gbcols + 1.U
    }
    when(state===sLineInc) {
      when(newgbcol){
        gblines := gblines + 1.U
      }.otherwise {
        gbpix := gbpix - GBWIDTH.U
      }
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
  if(stripped) {
    when(gb_display && (state===sPixInc)){
      when(((hvsync.io.hpos % 3.U) === 0.U) || (hvsync.io.vpos % 3.U === 0.U)) {
        io.vga_color := GbColors("b00".U)
      }.otherwise {
        io.vga_color := GbColors(io.mem_data)
      }
    }
  } else {
    when(gb_display && (state===sPixInc)){
      io.vga_color := GbColors(io.mem_data)
    }
  }

  /* Memory interface */
  io.mem_addr  := gbpix + 1.U
  io.mem_read  := true.B

}

object MemVgaZoomDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new MemVgaZoom())))
}
