package dev.vs.entrypoint

import dev.vs.adapter.input.config.AppConfig
import dev.vs.adapter.input.config.AppInfo
import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.Server
import dev.vs.adapter.input.http.interceptor.CtxInterceptor
import dev.vs.adapter.input.http.system.SystemEndpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.liveness.MetricsEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import dev.vs.adapter.output.ServerLogZioImpl
import dev.vs.adapter.output.ServerLogZioImpl.ServerLogZIO
import dev.vs.adapter.output.log.LogZioImpl
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import logstage.LogZIO
import logstage.LogZIO.log
import zio._
import zio.metrics.connectors.micrometer
import zio.metrics.connectors.micrometer.MicrometerConfig

object Main extends ZIOAppDefault:

  type ProgramEnv = LogZIO & CtxInterceptor & ServerLogZIO & AppConfig & SystemEndpoints

  private def program(): ZIO[ProgramEnv, Throwable, Unit] =
    for {
      _ <- log.info("Application is starting")
      appConfig <- ZIO.service[AppConfig]
      systemEndpoints <- ZIO.service[SystemEndpoints]
      systemServer <- ZIO.scoped {
        Server.run(systemEndpoints, appConfig.server.system)(runtime) *> ZIO.never
      }
      _ <- log.info("Application is stopped")
    } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    program().provide(
      ZLayer.succeed(MicrometerConfig.default),
      ZLayer.succeed(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)),
      micrometer.micrometerLayer,
      Runtime.enableRuntimeMetrics,
      // DefaultJvmMetrics.live.unit,
      AppInfo.live(Info.name, Info.version),
      AppConfig.live,
      LogZioImpl.live,
      CtxInterceptor.live,
      ServerLogZioImpl.live,
      BaseEndpoints.live,
      LivenessEndpoint.live,
      ReadinessEndpoint.live,
      MetricsEndpoint.live,
      SystemEndpoints.live
    )
  }
