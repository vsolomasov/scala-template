ThisBuild / scalaVersion := "3.4.1"
ThisBuild / version := "dev"

lazy val entrypoint = project
  .in(file("."))
  .settings(
    name := "template-zio",
    libraryDependencies ++= Dependencies.entrypointDependencies
  )
