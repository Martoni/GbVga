package gbvga

import chisel3._
import chisel3.util._
import chisel3.formal._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import GbConst._

class GbWrite (val datawidth: Int = 8,
               val input_sync: Boolean = true,
               val aformal: Boolean = false) extends Module {//with Formal {
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

  val shsync = if(input_sync) ShiftRegister(io.GBHsync,2) else io.GBHsync
  val svsync = if(input_sync) ShiftRegister(io.GBVsync,2) else io.GBVsync
  val sclk   = if(input_sync) ShiftRegister(io.GBClk  ,2) else io.GBClk
  val sdata  = if(input_sync) ShiftRegister(io.GBData ,2) else io.GBData

  val lineCount = RegInit(0.U(log2Ceil(GBHEIGHT).W))
  val pixelCount = RegInit((GBHEIGHT*GBWITH).U)
  val byteCount = RegInit(0.U(log2Ceil(GBWITH/4).W))

  val countreg = RegInit(0.U(12.W))
  io.countcol := pixelCount

  val pixelZero = VecInit(Seq.fill(4)(0.U(2.W)))
  val pixel = RegInit(pixelZero)

  /* Reset lines an column on vsync */
  when(risingedge(svsync)) {
    lineCount := 0.U
    byteCount := 0.U
    countreg := 0.U
    pixelCount := 0.U
  }

  /* change lines on GBHsync */
  when(fallingedge(shsync)) {
    lineCount := lineCount + 1.U
    byteCount := 0.U
    countreg := 0.U
  }

  /* read pixel on sclk fall */
  io.Mwrite := false.B
  when(fallingedge(sclk)) {
    pixel(pixelCount & 3.U) := sdata
    pixelCount := pixelCount + 1.U
    countreg := countreg + 1.U
    when((pixelCount & 3.U) === 0.U) {
      io.Mwrite := true.B
    }
  }

//  if(aformal){
//      past(io.Mwrite, 1) (pMwrite => {
//        when(io.Mwrite === true.B) {
//          assert(pMwrite === false.B)
//        }
//      })
//      cover(countreg === 10.U)
//  }

//  io.Maddr := (lineCount - 1.U)*GBWITH.U + pixelCount
  io.Maddr := pixelCount >> 2
  io.Mdata := pixel.asUInt
}

object GbWriteDriver extends App {
  (new ChiselStage).execute(args,
      Seq(ChiselGeneratorAnnotation(() => new GbWrite(8))))
}

object GbWriteFormal extends App {
  (new ChiselStage).execute(Array("-X", "sverilog"),
      Seq(ChiselGeneratorAnnotation(() => new GbWrite(8, aformal=true))))
}
