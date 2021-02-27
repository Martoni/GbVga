// See README.md for license details.

scalaVersion     := "2.12.12"
version          := "0.1.0"
organization     := "eu.fabienm"

lazy val root = (project in file("."))
  .settings(
    name := "gbvga",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % "3.4.2",
      "edu.berkeley.cs" %% "chiseltest" % "0.3.2" % "test",
      "edu.berkeley.cs" %% "chisel-iotesters" % "1.3.+"
//      "edu.berkeley.cs" %% "chisel-formal" % "0.1"
    ),
    scalacOptions ++= Seq(
      "-Xsource:2.11",
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit"
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.4.2" cross CrossVersion.full),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )
