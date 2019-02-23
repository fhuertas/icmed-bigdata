import sbt._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / organization     := "com.fhuertas.icemd.bigdata2019"
ThisBuild / organizationName := "icmed"

  lazy val root = project
  .in(file("."))
  .settings(settings)
