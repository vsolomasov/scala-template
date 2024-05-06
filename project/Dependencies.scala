import sbt.*

object Dependencies {

  private object Version {
    val zio = "2.0.22"
    val zioConfig = "4.0.2"
    val zioMetrics = "2.3.1"
    val micrometer = "1.12.5"
    val izumi = "1.2.8"
    val tapir = "1.10.6"
  }

  sealed trait Dependencies {
    def all: Seq[ModuleID]
  }

  private object ZIO extends Dependencies {
    val Core = "dev.zio" %% "zio" % Version.zio
    val Config = "dev.zio" %% "zio-config" % Version.zioConfig
    val Typesafe = "dev.zio" %% "zio-config-typesafe" % Version.zioConfig
    val Magnolia = "dev.zio" %% "zio-config-magnolia" % Version.zioConfig
    val all = Core :: Config :: Typesafe :: Magnolia :: Nil
  }

  private object ZioMetrics extends Dependencies {
    val Connectors = "dev.zio" %% "zio-metrics-connectors" % Version.zioMetrics
    val Micrometer = "dev.zio" %% "zio-metrics-connectors-micrometer" % Version.zioMetrics
    val all = Connectors :: Micrometer :: Nil
  }

  private object Micrometer extends Dependencies {
    val Core = "io.micrometer" % "micrometer-core" % Version.micrometer
    val Prometheus = "io.micrometer" % "micrometer-registry-prometheus" % Version.micrometer
    val all = Core :: Prometheus :: Nil
  }

  private object Izumi extends Dependencies {
    val LogStageCore = "io.7mind.izumi" %% "logstage-core" % Version.izumi
    val LogStageCirce = "io.7mind.izumi" %% "logstage-rendering-circe" % Version.izumi
    val LogStageSlf4jAdapter = "io.7mind.izumi" %% "logstage-adapter-slf4j" % Version.izumi
    val all = LogStageCore :: LogStageCirce :: LogStageSlf4jAdapter :: Nil
  }

  private object Tapir extends Dependencies {
    val Core = "com.softwaremill.sttp.tapir" %% "tapir-core" % Version.tapir
    val Zio = "com.softwaremill.sttp.tapir" %% "tapir-zio" % Version.tapir
    val Circe = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Version.tapir
    val Swagger = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % Version.tapir
    val ZioMetrics = "com.softwaremill.sttp.tapir" %% "tapir-zio-metrics" % Version.tapir
    val Vertx = "com.softwaremill.sttp.tapir" %% "tapir-vertx-server-zio" % Version.tapir
    val all = Core :: Zio :: Circe :: Swagger :: ZioMetrics :: Vertx :: Nil
  }

  val adapterDependencies = (ZIO :: ZioMetrics :: Micrometer :: Izumi :: Tapir :: Nil).flatMap(_.all)
}
