package gbvga

import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._

class VgaColors extends Bundle {
  val red   = UInt(6.W)
  val green = UInt(6.W)
  val blue  = UInt(6.W)
}

class Gb extends Bundle {
  val hsync = Bool()
  val vsync = Bool()
  val clk   = Bool()
  val data  = UInt(2.W)
}

trait GbConst { self: RawModule =>
  val GBWIDTH   = 160
  val GBHEIGHT = 144

  def risingedge(x: Bool) = x && !RegNext(x)
  def fallingedge(x: Bool) = !x && RegNext(x)

                            /* "#9BBC0F"*/
val GB_GREEN0 = (new VgaColors()).Lit(_.red   -> "h26".U(6.W),
                                      _.green -> "h2F".U(6.W),
                                      _.blue  -> "h03".U(6.W))

                          /* "#8BAC0F"*/
 val GB_GREEN1 = (new VgaColors()).Lit(_.red   -> "h1E".U(6.W),
                                       _.green -> "h27".U(6.W),
                                       _.blue  -> "h03".U(6.W))
                          /* "#306230"*/
 val GB_GREEN2 = (new VgaColors()).Lit(_.red   -> "h0C".U(6.W),
                                       _.green -> "h18".U(6.W),
                                       _.blue  -> "h0C".U(6.W))
                          /*"#0F380F"*/
 val GB_GREEN3 = (new VgaColors()).Lit(_.red   -> "h03".U(6.W),
                                       _.green -> "h0E".U(6.W),
                                       _.blue  -> "h03".U(6.W))

  val GbColors = VecInit(Array(GB_GREEN0, GB_GREEN1, GB_GREEN2, GB_GREEN3))


  val VGA_BLACK = (new VgaColors).Lit(_.red   -> 0.U(6.W),
                                      _.green -> 0.U(6.W),
                                      _.blue  -> 0.U(6.W))

  val VGA_WHITE = (new VgaColors).Lit(_.red   -> "b111111".U(6.W),
                                      _.green -> "b111111".U(6.W),
                                      _.blue  -> "b111111".U(6.W))
}
