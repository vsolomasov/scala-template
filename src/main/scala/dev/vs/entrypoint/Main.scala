package dev.vs.entrypoint

import dev.vs.adapter.input.config.AppConfig
import dev.vs.adapter.input.config.AppInfo
import dev.vs.adapter.input.http.BaseEndpoints
import dev.vs.adapter.input.http.interceptor.ctx.CtxInterceptor
import dev.vs.adapter.input.http.interceptor.log.ServerLogInterceptor
import dev.vs.adapter.input.http.interceptor.metric.ServerMetricInterceptor
import dev.vs.adapter.input.http.server.Server
import dev.vs.adapter.input.http.server.VertxServer
import dev.vs.adapter.input.http.system.SystemEndpoints
import dev.vs.adapter.input.http.system.liveness.LivenessEndpoint
import dev.vs.adapter.input.http.system.liveness.MetricsEndpoint
import dev.vs.adapter.input.http.system.readiness.ReadinessEndpoint
import dev.vs.adapter.output.log.LogZioImpl
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import logstage.LogZIO
import logstage.LogZIO.log
import zio._
import zio.metrics.connectors.micrometer
import zio.metrics.connectors.micrometer.MicrometerConfig

object Main extends ZIOAppDefault:

  type ProgramEnv = LogZIO & CtxInterceptor & ServerLogInterceptor & ServerMetricInterceptor &
    Server & AppConfig & SystemEndpoints

  private def program(): ZIO[ProgramEnv, Throwable, Unit] =
    for {
      _ <- log.info("Application is starting")
      server <- ZIO.service[Server]
      appConfig <- ZIO.service[AppConfig]
      systemEndpoints <- ZIO.service[SystemEndpoints]
      _ <- ZIO.scoped {
        server.run(appConfig.server.system, systemEndpoints)(runtime) *> ZIO.never
      }
      _ <- log.info("Application is stopped")
    } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    program().provide(
      ZLayer.succeed(MicrometerConfig.default),
      ZLayer.succeed(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)),
      micrometer.micrometerLayer,
      // Runtime.enableRuntimeMetrics,
      // DefaultJvmMetrics.live.unit,
      ServerMetricInterceptor.live,
      AppInfo.live(Info.name, Info.version),
      AppConfig.live,
      LogZioImpl.live,
      ServerLogInterceptor.live,
      CtxInterceptor.live,
      VertxServer.live,
      BaseEndpoints.live,
      LivenessEndpoint.live,
      ReadinessEndpoint.live,
      MetricsEndpoint.live,
      SystemEndpoints.live
    )
  }
