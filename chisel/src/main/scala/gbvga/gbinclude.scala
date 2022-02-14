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
                                        /* "#306230"*/
  val GB_GREEN1 = (new VgaColors()).Lit(_.red   -> "h0C".U(6.W),
                                        _.green -> "h18".U(6.W),
                                        _.blue  -> "h0C".U(6.W))
                                        /* "#8BAC0F"*/
  val GB_GREEN2 = (new VgaColors()).Lit(_.red   -> "h1E".U(6.W),
                                        _.green -> "h27".U(6.W),
                                        _.blue  -> "h03".U(6.W))
                                        /*"#0F380F"*/
  val GB_GREEN3 = (new VgaColors()).Lit(_.red   -> "h03".U(6.W),
                                        _.green -> "h0E".U(6.W),
                                        _.blue  -> "h03".U(6.W))

  val GbColors = VecInit(Array(GB_GREEN0, GB_GREEN1, GB_GREEN2, GB_GREEN3))

                                         /* "#FFFFFF"*/
  val GB_POCKET0 = (new VgaColors()).Lit(_.red   -> "h3F".U(6.W),
                                         _.green -> "h3F".U(6.W),
                                         _.blue  -> "h3F".U(6.W))
                                         /* "#AAAAAA"*/
  val GB_POCKET1 = (new VgaColors()).Lit(_.red   -> "h2A".U(6.W),
                                         _.green -> "h2A".U(6.W),
                                         _.blue  -> "h2A".U(6.W))
                                         /* "#555555"*/
  val GB_POCKET2 = (new VgaColors()).Lit(_.red   -> "h15".U(6.W),
                                         _.green -> "h15".U(6.W),
                                         _.blue  -> "h15".U(6.W))
                                         /*"#000000"*/
  val GB_POCKET3 = (new VgaColors()).Lit(_.red   -> "h00".U(6.W),
                                         _.green -> "h00".U(6.W),
                                         _.blue  -> "h00".U(6.W))

  val GbPocket = VecInit(Array(GB_POCKET0, GB_POCKET1, GB_POCKET2, GB_POCKET3))

                                        /* "#f4a7e1" */
  val GB_PINK0 =  (new VgaColors()).Lit(_.red   -> "h3d".U(6.W),
                                        _.green -> "h29".U(6.W),
                                        _.blue  -> "h38".U(6.W))
                                        /* "#f84ece" */
  val GB_PINK1 =  (new VgaColors()).Lit(_.red   -> "h3e".U(6.W),
                                        _.green -> "h13".U(6.W),
                                        _.blue  -> "h33".U(6.W))
                                         /* "#c40695" */
  val GB_PINK2 =  (new VgaColors()).Lit(_.red   -> "h31".U(6.W),
                                        _.green -> "h01".U(6.W),
                                        _.blue  -> "h25".U(6.W))
                                        /* "#6f0455" */
  val GB_PINK3 =  (new VgaColors()).Lit(_.red   -> "h1B".U(6.W),
                                        _.green -> "h01".U(6.W),
                                        _.blue  -> "h15".U(6.W))

  val GbPink = VecInit(Array(GB_PINK0, GB_PINK1, GB_PINK2, GB_PINK3))

  val GB_NIMP0 =  (new VgaColors()).Lit(_.red   -> "h3F".U(6.W),
                                        _.green -> "h3F".U(6.W),
                                        _.blue  -> "h3F".U(6.W))
  val GB_NIMP1 =  (new VgaColors()).Lit(_.red   -> "h00".U(6.W),
                                        _.green -> "h3F".U(6.W),
                                        _.blue  -> "h3F".U(6.W))
  val GB_NIMP2 =  (new VgaColors()).Lit(_.red   -> "h00".U(6.W),
                                        _.green -> "h00".U(6.W),
                                        _.blue  -> "h3F".U(6.W))
  val GB_NIMP3 =  (new VgaColors()).Lit(_.red   -> "h00".U(6.W),
                                        _.green -> "h00".U(6.W),
                                        _.blue  -> "h00".U(6.W))

  val GbNimp = VecInit(Array(GB_NIMP0, GB_NIMP1, GB_NIMP2, GB_NIMP3))

  val VGA_BLACK = (new VgaColors).Lit(_.red   -> 0.U(6.W),
                                      _.green -> 0.U(6.W),
                                      _.blue  -> 0.U(6.W))

  val VGA_WHITE = (new VgaColors).Lit(_.red   -> "b111111".U(6.W),
                                      _.green -> "b111111".U(6.W),
                                      _.blue  -> "b111111".U(6.W))
}
