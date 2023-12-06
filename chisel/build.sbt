// See README.md for license details.

scalaVersion     := "2.13.8"
version          := "0.1.1"
organization     := "eu.fabienm"

val majorChiselVersion = "3"
val minorChiselVersion = "5.6"

val chiselVersion = majorChiselVersion + "." + minorChiselVersion


lazy val root = (project in file("."))
  .settings(
    name := "gbvga",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % ("0."+minorChiselVersion) % "test",
    ),
    scalacOptions ++= Seq(
      "-Xsource:2.11",
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      // Enables autoclonetype2 in 3.4.x (on by default in 3.5)
      "-P:chiselplugin:useBundlePlugin"
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
