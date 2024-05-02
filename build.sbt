val projectName = "scala-template"

ThisBuild / version := "dev"
ThisBuild / organization := "dev.vs"
ThisBuild / scalaVersion := "3.4.1"
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
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "entrypoint",
    buildInfoObject := "Info",
    buildInfoPackage := s"${organization.value}.${name.value}",
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      BuildInfoKey("name" -> projectName)
    ),
    run / fork := true,
    run / javaOptions += s"-D${projectName}.inst=local",
    run / javaOptions += s"-D${projectName}.env=local",
  )
  .dependsOn(adapter)
  .aggregate(adapter)
