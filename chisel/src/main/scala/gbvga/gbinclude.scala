package gbvga

import chisel3._
import chisel3.util._

object GbConst{
  val GBWITH   = 160
  val GBHEIGHT = 144

  def risingedge(x: Bool) = x && !RegNext(x)
  def fallingedge(x: Bool) = !x && RegNext(x)
}
