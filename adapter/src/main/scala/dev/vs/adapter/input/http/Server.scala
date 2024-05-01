package dev.vs.adapter.input.http

import dev.vs.adapter.input.config.AppConfig.ServerConfig
import dev.vs.adapter.input.http.interceptor.CtxInterceptor
import dev.vs.adapter.output.ServerLogZioImpl.ServerLogZIO
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter
import sttp.tapir.server.vertx.zio.VertxZioServerInterpreter._
import sttp.tapir.server.vertx.zio.VertxZioServerOptions
import zio._

object Server:

  type ServerEnv = Scope & CtxInterceptor & ServerLogZIO

  def run[R](
    endpoints: Endpoints,
    server: ServerConfig
  )(implicit
    runtime: Runtime[R]
  ): ZIO[ServerEnv, Throwable, Unit] = {
    for {
      runtime <- ZIO.runtime
      vertx <- ZIO.succeed(Vertx.vertx())
      router <- ZIO.succeed(Router.router(vertx))
      serverLog <- ZIO.service[ServerLogZIO]
      ctxInterceptor <- ZIO.service[CtxInterceptor]
      _ <- ZIO.attempt {
        val serverOptions =
          VertxZioServerOptions
            .customiseInterceptors[Any]
            .prependInterceptor(ctxInterceptor)
            .serverLog(serverLog)
            .options
        val interpreter = VertxZioServerInterpreter[Any](serverOptions)
        endpoints.endpoints.foreach(interpreter.route(_)(runtime)(router))
      }
      server <- ZIO.attempt {
        val httpServerOptions = new HttpServerOptions()
        val httpServer = vertx.createHttpServer(
          httpServerOptions
            .setHost(server.host)
            .setPort(server.port)
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
