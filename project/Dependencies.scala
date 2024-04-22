import sbt.*

object Dependencies {

  private object Version {
    val zio = "2.0.22"
    val munit = "0.7.29"
  }

  private object ZIO {
    val Core = "dev.zio" %% "zio" % Version.zio
  }

  private object ScalaMeta {
    val Munit = "org.scalameta" %% "munit" % Version.munit % Test
  }

  val entrypointDependencies = ZIO.Core :: ScalaMeta.Munit :: Nil
}
