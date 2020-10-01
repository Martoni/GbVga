package gbvga

import chisel3._
import chisel3.util._

class GbVga extends Module {
  val io = IO(new Bundle {
  // Input Game boy signals
   /* GameBoy input */
    val GBHsync    = Input(Bool())
    val GBVsync    = Input(Bool())
    val GBClk      = Input(Bool())
    val GBData     = Input(UInt(2.W))

  // output VGA
  })
}
