package dev.vs.entrypoint

import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Server
import dev.vs.adapter.input.http.system.SystemEndpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio._

object Main extends ZIOAppDefault:

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    (for {
      systemEndpoints <- ZIO.service[SystemEndpoints]
      _ <- Server.run(systemEndpoints.endpoints.head)(runtime).exitCode
    } yield ()).provide(
      BaseEndpoints.live,
      LivenessEndpoint.live,
      SystemEndpoints.live
    )
  }
