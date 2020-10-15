package gbvga

import chisel3.formal.FormalSpec

class GbWriteFormalSpec extends FormalSpec {
  Seq(
    () => new GbWrite(8, aformal=true)
  ).map(verify(_))
}
