package dev.vs.adapter.input.http.server

import dev.vs.adapter.input.config.AppConfig.ServerConfig
import dev.vs.adapter.input.http.Endpoints
import dev.vs.adapter.input.http.interceptor.ctx.CtxInterceptor
import dev.vs.adapter.input.http.interceptor.log.ServerLogInterceptor
import dev.vs.adapter.input.http.interceptor.metric.ServerMetricInterceptor
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter._
import sttp.tapir.server.vertx.zio.VertxZioServerOptions
import zio._

class VertxServer(
  ctxInterceptor: CtxInterceptor,
  serverLogInterceptor: ServerLogInterceptor,
  serverMetricInterceptor: ServerMetricInterceptor
) extends Server:

  def run[R](
    config: ServerConfig,
    endpoints: Endpoints
  )(implicit
    runtime: Runtime[R]
  ): ZIO[Scope, Throwable, Unit] = {
    for {
      runtime <- ZIO.runtime
      vertx <- ZIO.succeed(Vertx.vertx())
      router <- ZIO.succeed(Router.router(vertx))
      _ <- ZIO.attempt {
        val serverOptions =
          VertxZioServerOptions
            .customiseInterceptors[Any]
            .prependInterceptor(ctxInterceptor)
            .metricsInterceptor(serverMetricInterceptor)
            .serverLog(serverLogInterceptor)
            .options
        val interpreter = VertxZioServerInterpreter[Any](serverOptions)
        endpoints.endpoints.foreach(interpreter.route(_)(runtime)(router))
      }
      server <- ZIO.attempt {
        val httpServerOptions = new HttpServerOptions()
        val httpServer = vertx.createHttpServer(
          httpServerOptions
            .setHost(config.host)
            .setPort(config.port)
        )
        httpServer.requestHandler(router)
      }
      _ <- ZIO.acquireRelease(
        ZIO.attempt(server.listen()).flatMap(_.asRIO)
      ) { server =>
        ZIO.attempt(server.close()).flatMap(_.asRIO).orDie
      }
    } yield ()
  }

object VertxServer:
  type ServerEnv = CtxInterceptor & ServerLogInterceptor & ServerMetricInterceptor

  def live: ZLayer[ServerEnv, Throwable, Server] = ZLayer.fromFunction(new VertxServer(_, _, _))
