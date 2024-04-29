package dev.vs.entrypoint

import dev.vs.adapter.input.config.AppConfig
import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Server
import dev.vs.adapter.input.http.system.SystemEndpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio._

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    (for {
      appConfig <- ZIO.service[AppConfig]
      systemEndpoints <- ZIO.service[SystemEndpoints]
      _ <- Server.run(systemEndpoints.endpoints, appConfig.server.system)(runtime)
    } yield ()).provide(
      AppConfig.live,
      BaseEndpoints.live,
      LivenessEndpoint.live,
      ReadinessEndpoint.live,
      SystemEndpoints.live
    )
  }
