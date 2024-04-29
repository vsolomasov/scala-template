import sbt.*

object Dependencies {

  private object Version {
    val zio = "2.0.22"
    val tapir = "1.10.6"
  }

  sealed trait Dependencies {
    def all: Seq[ModuleID]
  }

  private object ZIO extends Dependencies {
    val Core = "dev.zio" %% "zio" % Version.zio
    val all = Core :: Nil
  }

  private object Tapir extends Dependencies {
    val Core = "com.softwaremill.sttp.tapir" %% "tapir-core" % Version.tapir
    val Zio = "com.softwaremill.sttp.tapir" %% "tapir-zio" % Version.tapir
    val Circe = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Version.tapir
    val Swager = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % Version.tapir
    val Vertx = "com.softwaremill.sttp.tapir" %% "tapir-vertx-server-zio" % Version.tapir
    val all = Core :: Zio :: Circe :: Swager :: Vertx :: Nil
  }

  val adapterDependencies = (ZIO :: Tapir :: Nil).flatMap(_.all)
}