package gbvga

import chisel3._
import chisel3.util._
//import chisel3.formal.Formal
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
    /* debug */
    val countcol = Output(UInt(32.W))
  })

  val lineCount = RegInit(0.U(log2Ceil(GBHEIGHT).W))
  val pixelCount = RegInit((GBHEIGHT*GBWITH).U)
  val byteCount = RegInit(0.U(log2Ceil(GBWITH/4).W))

  val countreg = RegInit(0.U(12.W))
  io.countcol := pixelCount

  val pixelZero = VecInit(Seq.fill(4)(0.U(2.W)))
  val pixel = RegInit(pixelZero)

  /* Reset lines an column on GBVsync */
  when(risingedge(io.GBVsync)) {
    lineCount := 0.U
    byteCount := 0.U
    countreg := 0.U
    pixelCount := 0.U
  }

  /* change lines on GBHsync */
  when(fallingedge(io.GBHsync)) {
    lineCount := lineCount + 1.U
    byteCount := 0.U
    countreg := 0.U
  }

  /* read pixel on GBClk fall */
  io.Mwrite := false.B
  when(fallingedge(io.GBClk)) {
    pixel(pixelCount & 3.U) := io.GBData
    pixelCount := pixelCount + 1.U
    countreg := countreg + 1.U
    when((pixelCount & 3.U) === 0.U) {
      io.Mwrite := true.B
    }
  }

//  io.Maddr := (lineCount - 1.U)*GBWITH.U + pixelCount
  io.Maddr := pixelCount >> 2
  io.Mdata := pixel.asUInt
}

object GbWriteDriver extends App {
  (new ChiselStage).execute(args,
      Seq(ChiselGeneratorAnnotation(() => new GbWrite(8))))
}
