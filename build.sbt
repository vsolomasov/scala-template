ThisBuild / scalaVersion := "3.4.1"
ThisBuild / version := "dev"
ThisBuild / organization := "dev.vs"
ThisBuild / semanticdbEnabled := true
ThisBuild / scalacOptions += "-Wunused:all"

lazy val adapter = project
  .in(file("adapter"))
  .settings(
    name := "adapter",
    libraryDependencies ++= Dependencies.adapterDependencies
  )

lazy val entrypoint = project
  .in(file("."))
  .settings(
    name := "entrypoint",
    run / fork := true
  )
  .dependsOn(adapter)
  .aggregate(adapter)
