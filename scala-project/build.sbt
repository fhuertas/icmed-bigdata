import java.text.SimpleDateFormat

import sbt._
import java.util.Calendar
ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / organization     := "com.fhuertas.icemd.bigdata2019"
ThisBuild / organizationName := "icmed"

  lazy val root = project
  .in(file("."))
  .settings(settings)

val format= new SimpleDateFormat("yyyyMMdd_HHmmss")
val currentTime = format.format(Calendar.getInstance().getTime)
ThisBuild / assemblyJarName in assembly := s"icemd-bigdata_$currentTime.jar"

ThisBuild / test in assembly := {}
