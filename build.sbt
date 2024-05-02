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
    run / fork := true,
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      BuildInfoKey.apply("name" -> projectName),
      BuildInfoKey.action("buildTime") {
        System.currentTimeMillis()
      }
    ),
    buildInfoPackage := s"${organization.value}.${name.value}"
  )
  .dependsOn(adapter)
  .aggregate(adapter)
