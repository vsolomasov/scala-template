package dev.vs.entrypoint

import dev.vs.adapter.input.config.AppConfig
import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Server
import dev.vs.adapter.input.http.system.SystemEndpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import dev.vs.adapter.output.Logger
import logstage.LogZIO
import logstage.LogZIO.log
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio._

object Main extends ZIOAppDefault:

  type ProgramEnv = LogZIO & AppConfig & SystemEndpoints

  private def program(): ZIO[ProgramEnv, Throwable, Unit] =
    for {
      _ <- log.info("Application is starting")
      appConfig <- ZIO.service[AppConfig]
      systemEndpoints <- ZIO.service[SystemEndpoints]
      systemServer <- ZIO.scoped {
        Server.run(systemEndpoints, appConfig.server.system)(runtime) *> ZIO.never
      }
      _ <- log.info("Application is stoppqed")
    } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    program().provide(
      AppConfig.live,
      Logger.live,
      BaseEndpoints.live,
      LivenessEndpoint.live,
      ReadinessEndpoint.live,
      SystemEndpoints.live
    )
  }
