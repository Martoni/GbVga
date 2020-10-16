package gbvga

/* Horizontal, vertical VGA signals generation
* for 640 x 480 VGA
* inspired from fpga4fun
* and Stephen Hugg book "Designing Video Game Hardware in Verilog"
* */

import chisel3._
import chisel3.util._
import chisel3.formal.Formal
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import GbConst._

object VGAConst {
/* Dimensions for 640x480 @ 60Hz
* found here http://martin.hinner.info/vga/640x480_60.html*/

  val H_DISPLAY = 640  // horizontal display width
  val H_FRONT = 8      // front porch
  val H_SYNC = 96      // sync width
  val H_BACK = 40      // back porch
  val
  val V_SYNC = 4       // sync width
  val V_BACK = 25      // back porch
  val V_TOP = 4        // top border
  val V_DISPLAY = 480  // vertical display width
  val V_BOTTOM = 14    // bottom border
  val H_SYNC_START = H_DISPLAY + H_FRONT
  val H_SYNC_END = H_DISPLAY + H_FRONT + H_SYNC - 1
  val H_MAX = H_DISPLAY + H_BACK + H_FRONT + H_SYNC - 1
  val V_SYNC_START = V_DISPLAY + V_BOTTOM
  val V_SYNC_END = V_DISPLAY + V_BOTTOM + V_SYNC - 1
  val V_MAX = V_DISPLAY + V_TOP + V_BOTTOM + V_SYNC - 1
}

class HVSync extends Module with Formal {
  val io = IO(new Bundle {
     val hsync = Output(Bool()) 
     val vsync = Output(Bool())
     val display_on = Output(Bool())
     val hpos = Output(UInt(10.W))
     val vpos = Output(UInt(9.W))
  })

}
