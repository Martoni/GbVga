package gbvga

import chisel3._
import chisel3.util._
import chisel3.formal._
import chisel3.formal.FormalSpec


class GbWriteFormal extends GbWrite(
    datawidth=2, debug_simu=false, aformal=false
  ) with Formal {
  /* Mwrite should be 1 cycle wide */
  past(io.Mwrite, 1) (pMwrite => {
    when(io.Mwrite === true.B) {
      assert(pMwrite === false.B)
    }
  })
  cover(countreg === 10.U)
}


class GbWriteFormalSpec extends FormalSpec {
  Seq(
    () => new GbWrite(8, aformal=true)
  ).map(verify(_))
}
