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

class HVSync extends Module { // with Formal { scala version problem
  val io = IO(new Bundle {
     val hsync = Output(Bool())
     val vsync = Output(Bool())
     val display_on = Output(Bool())
     val hpos = Output(UInt(10.W))
     val vpos = Output(UInt(9.W))
  })

  val H_DISPLAY = 640.U  // horizontal display width
  val H_FRONT = 8.U      // front porch
  val H_SYNC = 96.U      // sync width
  val H_BACK = 40.U      // back porch
  val V_SYNC = 4.U       // sync width
  val V_BACK = 25.U      // back porch
  val V_TOP = 4.U        // top border
  val V_DISPLAY = 480.U  // vertical display width
  val V_BOTTOM = 14.U    // bottom border
  val H_SYNC_START = H_DISPLAY + H_FRONT
  val H_SYNC_END = H_DISPLAY + H_FRONT + H_SYNC - 1.U
  val H_MAX = H_DISPLAY + H_BACK + H_FRONT + H_SYNC - 1.U
  val V_SYNC_START = V_DISPLAY + V_BOTTOM
  val V_SYNC_END = V_DISPLAY + V_BOTTOM + V_SYNC - 1.U
  val V_MAX = V_DISPLAY + V_TOP + V_BOTTOM + V_SYNC - 1.U

  val vpos_count = RegInit(0.U(9.W))
  val hpos_count = RegInit(0.U(10.W))
  io.vpos := vpos_count
  io.hpos := hpos_count

  io.display_on := (hpos_count < H_DISPLAY) && (vpos_count < V_DISPLAY)

  /* Horizontal counter */
  io.hsync := !((hpos_count >= H_SYNC_START) &&
                (hpos_count <= H_SYNC_END))

  val hpos_max = hpos_count === H_MAX
  val vpos_max = vpos_count === V_MAX

  hpos_count := hpos_count + 1.U
  when(hpos_max){
    hpos_count := 0.U
  }

  /* Vertical counter */
  io.vsync := !((vpos_count >= V_SYNC_START) &&
              (vpos_count <= V_SYNC_END))
  when(hpos_max) {
    vpos_count := vpos_count + 1.U
    when(vpos_max) {
      vpos_count := 0.U
    }
  }
}

object HVSyncDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new HVSync())))
}
