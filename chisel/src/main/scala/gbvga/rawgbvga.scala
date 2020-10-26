package gbvga

/* direct connection GB -> VGA for tests only
 * Fabien Marteau <mail@fabienm.eu>
 */

import chisel3._
import chisel3.util._
import chisel3.formal.Formal
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import GbConst._

class RawGbVga extends RawModule {

  /************/
  /** outputs */
  /* Clock and reset */
  val clock = IO(Input(Clock()))
  val resetn = IO(Input(Bool()))
  val pll_rstn = IO(Output(Bool()))

  /* game boy signals */
  val gb_hsync = IO(Input(Bool()))
  val gb_vsync = IO(Input(Bool()))
  val gb_clk = IO(Input(Bool()))
  val gb_data = IO(Input(UInt(2.W)))

  /* Vga */
  val hsync = IO(Output(Bool()))
  val vsync = IO(Output(Bool()))
  val red = IO(Output(UInt(6.W)))
  val green = IO(Output(UInt(6.W)))
  val blue = IO(Output(UInt(6.W)))


  withClockAndReset(clock, ~resetn) {
    /* Activate pll at start*/
    pll_rstn := true.B

    /* synchronize gb signals */
    val gbclk_s = ShiftRegister(gb_clk, 2)
     

    /* colors regs */
    val red_reg = RegInit(0.U(6.W))
    val green_reg = RegInit(0.U(6.W))
    val blue_reg = RegInit(0.U(6.W))
    red := red_reg
    green := green_reg
    blue := blue_reg


    /* latching gbpixel */
    val gbpixlatch = RegInit(0.U(2.W))
    when(fallingedge(gbclk_s)){
      gbpixlatch := gb_data //XXX gb_data not synchronized
    }

    /* Module connections */
    val hvsync = Module(new HVSync)
    hsync := hvsync.io.hsync
    vsync := hvsync.io.vsync

    /* print color */
    /*COLOR = ["#0F380F", "#8BAC0F", "#306230", "#9BBC0F"]*/
    red_reg   := 0.U
    green_reg := 0.U
    blue_reg  := 0.U
    when(hvsync.io.display_on) {
      switch(gbpixlatch) {
        is("b00".U) {
          red_reg   := "h26".U
          green_reg := "h2F".U
          blue_reg  := "h03".U
        }
        is("b01".U) {
          red_reg   := "h1E".U
          green_reg := "h27".U
          blue_reg  := "h03".U
        }
        is("b10".U) {
          red_reg   := "h0C".U
          green_reg := "h18".U
          blue_reg  := "h0C".U
        }
        is("b11".U) {
          red_reg   := "h03".U
          green_reg := "h0E".U
          blue_reg  := "h03".U
        }

      }
    }
  }

}

object RawGbVgaDriver extends App {
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new RawGbVga())))
}
