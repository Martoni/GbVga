package gbvga

import chisel3._
import chisel3.util._
import chisel3.formal.Formal
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
//import chisel3.experimental.{verification => formal}

import GbConst._

class GbWrite (val datawidth: Int = 8) extends Module { //with Formal {
  val io = IO(new Bundle {
    /* GameBoy input */
    val GBHsync    = Input(Bool())
    val GBVsync    = Input(Bool())
    val GBClk      = Input(Bool())
    val GBData     = Input(UInt(2.W))
    /* Memory write */
    val Maddr  = Output(UInt((log2Ceil(GBWITH*GBHEIGHT*2/datawidth)).W))
    val Mdata  = Output(UInt(datawidth.W))
    val Mwrite = Output(Bool())
  })

  /*XXX change this */
  io.Maddr  := DontCare
  io.Mdata  := DontCare
  io.Mwrite := DontCare

//  past(io.Mdata, 1) (pastIOMdata => {
//    assert(io.Mdata >= pastIOMdata)
//  })
}

object GbWriteDriver extends App {
  (new ChiselStage).execute(args,
      Seq(ChiselGeneratorAnnotation(() => new GbWrite(8))))
}
